package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigServiceGrpc;
import tig.grpc.server.data.dao.AuthenticationDAO;
import tig.grpc.server.data.dao.FileDAO;
import tig.grpc.server.data.dao.UsersDAO;
import tig.grpc.server.session.SessionAuthenticator;

import java.util.Arrays;
import java.util.List;

public class TigServiceImpl extends TigServiceGrpc.TigServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigServiceImpl.class);

    @Override
    public void register(Tig.AccountRequest request, StreamObserver<Empty> responseObserver) {
        logger.info(String.format("Register username: %s", request.getUsername()));

        UsersDAO.insertUser(request.getUsername(), request.getPassword());

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(Tig.AccountRequest request, StreamObserver<Tig.LoginReply> responseObserver) {
        logger.info(String.format("Login username: %s", request.getUsername()));

        UsersDAO.authenticateUser(request.getUsername(), request.getPassword());
        String sessionId = SessionAuthenticator.createSession(request.getUsername());

        Tig.LoginReply.Builder builder = Tig.LoginReply.newBuilder().setSessionId(sessionId);

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void logout(Tig.SessionRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Logout");

        SessionAuthenticator.clearSession(request.getSessionId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteFile(Tig.DeleteFileRequest request, StreamObserver<Empty> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());

        logger.info(String.format("Delete filename: %s of users %s", request.getFilename(), username));

        FileDAO.deleteFile(username, request.getFilename());

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void accessControlFile(Tig.AccessControlRequest request, StreamObserver<Empty> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());

        logger.info(String.format("Access Control from file %s of user %s to user %s and make it: %s", request.getFileName(),
                username, request.getTarget(), request.getPermission()));


        AuthenticationDAO.updateAccessControl(request.getFileName(), username, request.getTarget(), request.getPermission().getNumber());

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void listFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        List<String> files = FileDAO.listFiles(username);
        logger.info("List files " + username);

        Tig.ListFilesReply.Builder builder = Tig.ListFilesReply.newBuilder();
        builder.addAllFileInfo(files);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public StreamObserver<Tig.FileChunkClientUpload> uploadFile(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Tig.FileChunkClientUpload>() {
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[]{});
            private String filename;
            private String username;
            private final Object lock = new Object();

            @Override
            public void onNext(Tig.FileChunkClientUpload value) {
                //Synchronize onNext calls by sequence
                synchronized (lock) {
                    while (counter != value.getSequence()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    //Renew Lease
                    username = SessionAuthenticator.authenticateSession(value.getSessionId());
                    if (counter == 0) {
                        filename = value.getFileName();
                    }

                    file = file.concat(value.getContent());
                    counter++;
                    lock.notify();
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
                FileDAO.fileUpload(filename, file.toByteArray(), username);
                responseObserver.onCompleted();
            }

        };

    }

    @Override
    public StreamObserver<Tig.FileChunkClientEdit> editFile(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Tig.FileChunkClientEdit>() {
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[0]);
            private String filename;
            private String owner;
            private final Object lock = new Object();

            @Override
            public void onNext(Tig.FileChunkClientEdit value) {
                //Synchronize onNext calls
                synchronized (lock) {
                    while (counter != value.getSequence()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    //Renew lease
                    String username = SessionAuthenticator.authenticateSession(value.getSessionId());
                    if (counter == 0) {
                        AuthenticationDAO.authenticateFileAccess(username, value.getFileName(), value.getOwner(), 1);
                        filename = value.getFileName();
                        owner = value.getOwner();
                    }

                    file = file.concat(value.getContent());
                    counter++;
                    lock.notify();
                }
            }


            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
                FileDAO.fileEdit(filename, file.toByteArray(), owner);
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    public void downloadFile(Tig.FileRequest request, StreamObserver<Tig.FileChunkDownload> responseObserver) {
        logger.info(String.format("Download file: %s", request.getFileName()));

        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        AuthenticationDAO.authenticateFileAccess(username, request.getFileName(), request.getOwner(), 0);

        byte[] file = FileDAO.getFileContent(request.getFileName(), request.getOwner());
        int sequence = 0;
        //Send file 1MB chunk at a time
        for (int i = 0; i < file.length; i += 1024 * 1024, sequence++) {
            int chunkSize = Math.min(1024 * 1024, file.length - i);
            Tig.FileChunkDownload.Builder builder = Tig.FileChunkDownload.newBuilder();
            builder.setContent(ByteString.copyFrom(Arrays.copyOfRange(file, i, i + chunkSize)));
            builder.setSequence(sequence);
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

}
