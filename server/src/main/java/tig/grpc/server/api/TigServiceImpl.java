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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TigServiceImpl extends TigServiceGrpc.TigServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigServiceImpl.class);

    @Override
    public void register(Tig.LoginRequest request, StreamObserver<Tig.StatusReply> responseObserver) {
        logger.info(String.format("Register username: %s", request.getUsername()));

        UsersDAO.insertUser(request.getUsername(), request.getPassword());

        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        builder.setCode(Tig.StatusCode.OK);

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(Tig.LoginRequest request, StreamObserver<Tig.LoginReply> responseObserver) {
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
    public void deleteFile(Tig.FileRequest request, StreamObserver<Tig.StatusReply> responseObserver) {
        logger.info(String.format("Delete filename: %s", request.getFileName()));
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        AuthenticationDAO.authenticateFileAccess(username, request.getFileName(), request.getOwner(), request.getPermission());

        FileDAO.deleteFile(username, request.getFileName(), request.getOwner());
        // FIXME e assim?
        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        builder.setCode(Tig.StatusCode.OK);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void accessControlFile(Tig.OperationRequest request, StreamObserver<Tig.StatusReply> responseObserver) {
        // FIXME !!! Da trabalhooo
        logger.info(String.format("Access Control from: %s and make it: %s", request.getFileName(), request.getOperation()));
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        AuthenticationDAO.authenticateFileAccess(username, request.getFileName(), request.getOwner(), request.getPermission());

        boolean flag = request.getOperation().equals("PUBLIC");
        AuthenticationDAO.updateAccessControl(username, request.getFileName(), flag);

        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        builder.setCode(Tig.StatusCode.OK);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {
        logger.info("List files");
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        List<String> files = FileDAO.listFiles(username);

        Tig.ListFilesReply.Builder builder = Tig.ListFilesReply.newBuilder();
        builder.addAllFileInfo(files);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public StreamObserver<Tig.FileChunk> uploadFile(StreamObserver<Tig.StatusReply> responseObserver) {
        return new StreamObserver<Tig.FileChunk>() {
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[] {});
            private String filename;
            private String username;
            private final Object lock = new Object();

            @Override
            public void onNext(Tig.FileChunk value) {
                //Synchronize onNext calls by sequence
                synchronized (lock) {
                    while (counter != value.getSequence()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    if (counter == 0) {
                        username = SessionAuthenticator.authenticateSession(value.getSessionId());
                        filename = value.getFileName();
                    }
                    logger.info(String.format("Upload file %s chunk %d", filename, value.getSequence()));

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
                responseObserver.onNext(Tig.StatusReply.newBuilder().setCode(Tig.StatusCode.OK).build());
                FileDAO.fileUpload(filename, file.toByteArray(), username);
                responseObserver.onCompleted();
            }

        };

    }

    @Override
    public StreamObserver<Tig.FileChunk> editFile(StreamObserver<Tig.StatusReply> responseObserver) {
        return new StreamObserver<Tig.FileChunk>() {
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[0]);
            private String username;
            private String filename;
            private String owner;
            private final Object lock = new Object();

            @Override
            public void onNext(Tig.FileChunk value) {
                //Synchronize onNext calls
                synchronized (lock) {
                    while (counter != value.getSequence()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    if (counter == 0) {
                        username = SessionAuthenticator.authenticateSession(value.getSessionId());
                        AuthenticationDAO.authenticateFileAccess(username, value.getFileName(), value.getOwner(), value.getPermission());
                    }

                    filename = value.getFileName();
                    owner = value.getOwner();

                    logger.info(String.format("Edit file %s chunk %d", filename, value.getSequence()));
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
                responseObserver.onNext(Tig.StatusReply.newBuilder().setCode(Tig.StatusCode.OK).build());
                FileDAO.fileEdit(filename, file.toByteArray(), owner);
            }

        };
    }

    @Override
    public void downloadFile(Tig.FileRequest request, StreamObserver<Tig.FileChunk> responseObserver) {
        //FIXME Test this
        logger.info(String.format("Download file: %s", request.getFileName()));

        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        AuthenticationDAO.authenticateFileAccess(username, request.getFileName(), request.getOwner(), request.getPermission());

        byte[] file = FileDAO.getFileContent(request.getFileName(), request.getOwner());

        //Send file 1kb chunk at a time
        for (int i = 0; i < file.length; i += 1024) {
            int chunkSize = Math.min(1024, file.length - i);
            Tig.FileChunk.Builder builder = Tig.FileChunk.newBuilder();
            builder.setFileName(request.getFileName());
            builder.setContent(ByteString.copyFrom(Arrays.copyOfRange(file, i, i + chunkSize)));
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

}
