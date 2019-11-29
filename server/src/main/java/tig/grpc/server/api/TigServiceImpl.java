package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.contract.TigServiceGrpc;
import tig.grpc.server.dao.FileDAO;
import tig.grpc.server.session.SessionAuthenticator;
import tig.utils.encryption.EncryptionUtils;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class TigServiceImpl extends TigServiceGrpc.TigServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigServiceImpl.class);
    public static TigKeyServiceGrpc.TigKeyServiceBlockingStub keyStub;
    public static TigBackupServiceGrpc.TigBackupServiceBlockingStub backupStub;

    @Override
    public void register(Tig.AccountRequest request, StreamObserver<Empty> responseObserver) {
        logger.info(String.format("Register username: %s", request.getUsername()));
        try {
            responseObserver.onNext(keyStub.registerTigKey(request));
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void login(Tig.AccountRequest request, StreamObserver<Tig.LoginReply> responseObserver) {
        logger.info(String.format("Login username: %s", request.getUsername()));
        try {
        Tig.LoginReply replyMessage = keyStub.loginTigKey(request);
        responseObserver.onNext(replyMessage);
        responseObserver.onCompleted();
        } catch(StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void logout(Tig.SessionRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Logout");
        try {
        responseObserver.onNext(keyStub.logoutTigKey(request));
        responseObserver.onCompleted();
        } catch(StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(Tig.DeleteFileRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Delete file");
        try {
            Tig.DeleteFileReply reply = keyStub.deleteFileTigKey(request);
            FileDAO.deleteFile(reply.getFileId());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void accessControlFile(Tig.AccessControlRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Access Control file");
        try {
            responseObserver.onNext(keyStub.accessControlFileTigKey(request));
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void listFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {
        logger.info("List files");
        try {
            Tig.ListFilesReply files = keyStub.listFileTigKey(Tig.TigKeySessionIdMessage.newBuilder(
                    Tig.TigKeySessionIdMessage.newBuilder().setSessionId(request.getSessionId()).build()).build());
            responseObserver.onNext(files);
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void listBackupFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {
        logger.info("List Backup files");
        try {
            Tig.ListFilesReply files = backupStub.listBackupFiles(Tig.ListBackupFilesRequest.newBuilder(Tig.ListBackupFilesRequest.newBuilder().setSessionId(request.getSessionId()).build()).build());
            responseObserver.onNext(files);
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public StreamObserver<Tig.FileChunkClientUpload> uploadFile(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Tig.FileChunkClientUpload>() {
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[]{});
            private String filename;
            private String sessionId;
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
                    if (counter == 0) {
                        filename = value.getFileName();
                        sessionId = value.getSessionId();
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
                Tig.NewFileReply reply = keyStub.newFileKey(Tig.NewFileRequest.newBuilder()
                        .setFilename(filename)
                        .setSessionId(Tig.TigKeySessionIdMessage.newBuilder().setSessionId(sessionId))
                        .build()
                );
                try {
                    responseObserver.onNext(Empty.newBuilder().build());
                    FileDAO.fileUpload(reply.getFileId(), file.toByteArray(), reply.getKey().toByteArray(), reply.getIv().toByteArray());
                    responseObserver.onCompleted();
                } catch (StatusRuntimeException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
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
            String sessionId;
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

                    if (counter == 0) {
                        sessionId = value.getSessionId();
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
                Tig.CanEditTigKeyReply reply;
                try {
                     reply = keyStub.canEditTigKey(Tig.KeyFileTigKeyRequest.newBuilder()
                            .setFilename(filename)
                            .setOwner(owner)
                            .setSessionId(Tig.TigKeySessionIdMessage.newBuilder().setSessionId(sessionId))
                            .build());
                } catch (StatusRuntimeException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }

                FileDAO.fileEdit(reply.getFileId(), file.toByteArray(), reply.getNewKeyFile().toByteArray(), reply.getIv().toByteArray());

                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void downloadFile(Tig.FileRequest request, StreamObserver<Tig.FileChunkDownload> responseObserver) {
        logger.info(String.format("Download file: %s", request.getFileName()));
        String sessionId = request.getSessionId();
        String filename = request.getFileName();
        String owner = request.getOwner();

        Tig.KeyFileTigKeyReply fileKeyReply;

        try {
            fileKeyReply = keyStub.keyFileTigKey(Tig.KeyFileTigKeyRequest.newBuilder()
                    .setFilename(filename)
                    .setOwner(owner)
                    .setSessionId(Tig.TigKeySessionIdMessage.newBuilder().setSessionId(sessionId).build())
                    .build());
        } catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        byte[] keyArray = fileKeyReply.getKey().toByteArray();
        byte[] iv = fileKeyReply.getIv().toByteArray();
        SecretKeySpec fileKey = EncryptionUtils.getAesKey(keyArray);

        byte[] file = FileDAO.getFileContent(fileKeyReply.getFileId(), fileKey, iv);

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
