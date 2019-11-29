package tig.grpc.keys.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.keys.dao.AuthenticationDAO;
import tig.grpc.keys.dao.FileDAO;
import tig.grpc.keys.dao.UsersDAO;
import tig.grpc.keys.session.SessionAuthenticator;
import tig.grpc.keys.session.UserToken;
import tig.utils.StringGenerator;
import tig.utils.encryption.FileKey;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.PasswordUtils;

import java.util.List;

public class TigKeyServiceImpl extends TigKeyServiceGrpc.TigKeyServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigKeyServiceImpl.class);

    @Override
    public void registerTigKey(Tig.AccountRequest request, StreamObserver<Empty> responseObserver) {
        logger.info(String.format("Register username: %s", request.getUsername()));
        PasswordUtils.validateNewPassword(request.getPassword());
        UsersDAO.insertUser(request.getUsername(), request.getPassword());

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

    }


    @Override
    public void loginTigKey(Tig.AccountRequest request, StreamObserver<Tig.LoginReply> reply) {
        logger.info(String.format("Login username: %s", request.getUsername()));

        UsersDAO.authenticateUser(request.getUsername(), request.getPassword());
        String sessionId = SessionAuthenticator.createSession(request.getUsername());

        Tig.LoginReply.Builder builder = Tig.LoginReply.newBuilder().setSessionId(sessionId);

        reply.onNext(builder.build());
        reply.onCompleted();
    }

    @Override
    public void logoutTigKey(Tig.SessionRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Logout no Key Server");
        SessionAuthenticator.clearSession(request.getSessionId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void keyFileTigKey(Tig.KeyFileTigKeyRequest request, StreamObserver<Tig.KeyFileTigKeyReply> reply) {
        logger.info(String.format("get key of file with name: %s of owner: %s", request.getFilename(), request.getOwner()));
        String sessionId = request.getSessionId().getSessionId();
        String filename = request.getFilename();
        String owner = request.getOwner();

        UserToken userToken = SessionAuthenticator.authenticateSession(sessionId);
        String username = userToken.getUsername();

        AuthenticationDAO.authenticateFileAccess(username, filename, owner, 0);

        FileKey fileKey = FileDAO.getFileEncryptionKey(filename, owner);
        Tig.KeyFileTigKeyReply replyMessage = Tig.KeyFileTigKeyReply.newBuilder()
                                              .setIv(ByteString.copyFrom(fileKey.getIv()))
                                              .setKey(ByteString.copyFrom(fileKey.getKey()))
                                              .setFileId(fileKey.getId())
                                              .build();

        reply.onNext(replyMessage);
        reply.onCompleted();
    }

    @Override
    public void canEditTigKey(Tig.KeyFileTigKeyRequest request, StreamObserver<Tig.CanEditTigKeyReply> reply) {
        logger.info(String.format("filename: %s, owner: %s, session id: %s", request.getFilename(), request.getOwner(), request.getSessionId()));

        String username = SessionAuthenticator.authenticateSession(request.getSessionId().getSessionId()).getUsername();
        AuthenticationDAO.authenticateFileAccess(username, request.getFilename(), request.getOwner(), 1);

        byte[] key = EncryptionUtils.generateAESKey().getEncoded();
        byte[] iv = EncryptionUtils.generateIv();

        String fileId = FileDAO.updateFileKey(new FileKey(key, iv, ""), request.getFilename(), request.getOwner());

        Tig.CanEditTigKeyReply.Builder builder = Tig.CanEditTigKeyReply.newBuilder();

        builder.setNewKeyFile(ByteString.copyFrom(key));
        builder.setIv(ByteString.copyFrom(iv));
        builder.setFileId(fileId);

        reply.onNext(builder.build());
        reply.onCompleted();

    }

    @Override
    public void deleteFileTigKey(Tig.DeleteFileRequest request, StreamObserver<Tig.DeleteFileReply> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId()).getUsername();
        logger.info(String.format("Delete filename: %s of users %s", request.getFilename(), username));
        String fileId = FileDAO.deleteFile(username, request.getFilename());
        responseObserver.onNext(Tig.DeleteFileReply.newBuilder().setFileId(fileId).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listFileTigKey(Tig.TigKeySessionIdMessage request, StreamObserver<Tig.ListFilesReply> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId()).getUsername();
        List<String> files = FileDAO.listFiles(username);
        logger.info("List files " + username);

        Tig.ListFilesReply.Builder builder = Tig.ListFilesReply.newBuilder();
        builder.addAllFileInfo(files);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public void accessControlFileTigKey(Tig.AccessControlRequest request, StreamObserver<Empty> responseObserver) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId()).getUsername();

        logger.info(String.format("Access Control from file %s of user %s to user %s and make it: %s", request.getFileName(),
                username, request.getTarget(), request.getPermission()));

        AuthenticationDAO.updateAccessControl(request.getFileName(), username, request.getTarget(), request.getPermission().getNumber());

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void newFileKey(Tig.NewFileRequest request, StreamObserver<Tig.NewFileReply> reply) {
        logger.info(String.format("newFileKey, file name %s", request.getFilename()));
        String username = SessionAuthenticator.authenticateSession(request.getSessionId().getSessionId()).getUsername();

        byte[] key = EncryptionUtils.generateAESKey().getEncoded();
        byte[] iv = EncryptionUtils.generateIv();

        String fileId = StringGenerator.randomString(256);

        FileDAO.createFileKey(new FileKey(key, iv, fileId), request.getFilename(), username);
        Tig.NewFileReply.Builder builder = Tig.NewFileReply.newBuilder();

        builder.setFileId(fileId);
        builder.setIv(ByteString.copyFrom(iv));
        builder.setKey(ByteString.copyFrom(key));
        reply.onNext(builder.build());
        reply.onCompleted();
    }



}
