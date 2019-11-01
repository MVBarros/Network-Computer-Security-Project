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

        UsersDAO.deleteFile(username, request.getFileName());

        // FIXME e assim?
        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        builder.setCode(Tig.StatusCode.OK);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void accessControlFile(Tig.OperationRequest request, StreamObserver<Tig.StatusReply> responseObserver) {
        logger.info(String.format("Access Control from: %s and make it: %s", request.getFileName(), request.getOperation()));
        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        boolean flag = request.getOperation().equals("PUBLIC");
        UsersDAO.updateAccessControl(username, request.getFileName(), flag);

        // FIXME confirmar
        Tig.StatusReply.Builder builder = Tig.StatusReply.newBuilder();
        builder.setCode(Tig.StatusCode.OK);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listFiles(Tig.SessionRequest request, StreamObserver<Tig.ListFilesReply> responseObserver) {

    }

    @Override
    public StreamObserver<Tig.FileChunk> uploadFile(StreamObserver<Tig.StatusReply> responseObserver) {

        return new StreamObserver<Tig.FileChunk>() {
            private int counter = 0;
            private ByteString file = ByteString.EMPTY;
            private String filename;

            @Override
            public void onNext(Tig.FileChunk value) {
                if (counter == 0)
                    SessionAuthenticator.authenticateSession(value.getSessionId());
                counter++;
                filename = value.getFileName();
                file.concat(value.getContent());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Tig.StatusReply.newBuilder().setCode(Tig.StatusCode.OK).build());
                FileDAO.fileUpload(filename, file);

            }

        };

    }

    @Override
    public StreamObserver<Tig.FileChunk> editFile(StreamObserver<Tig.StatusReply> responseObserver) {
        return null;
    }

    @Override
    public void downloadFile(Tig.FileRequest request, StreamObserver<Tig.FileChunk> responseObserver) {
        //FIXME Test this
        logger.info(String.format("Download file: %s", request.getFileName()));

        String username = SessionAuthenticator.authenticateSession(request.getSessionId());
        AuthenticationDAO.authenticateFileAccess(username, request.getFileName());

        byte[] file = FileDAO.getFileContent(request.getFileName());

        //Send file 1kb chunk at a time
        for(int i = 0; i < file.length; i += 1024) {
            int chunkSize = Math.min(1024, file.length - 1024);
            Tig.FileChunk.Builder builder = Tig.FileChunk.newBuilder();
            builder.setFileName(request.getFileName());
            builder.setContent(ByteString.copyFrom(Arrays.copyOfRange(file, i, i + chunkSize)));
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

}
