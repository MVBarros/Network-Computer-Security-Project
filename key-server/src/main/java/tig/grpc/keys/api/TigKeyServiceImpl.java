package tig.grpc.keys.api;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.keys.dao.AuthenticationDAO;
import tig.grpc.keys.dao.UsersDAO;
import tig.grpc.keys.session.SessionAuthenticator;
import tig.utils.encryption.EncryptionUtils;

import javax.crypto.SecretKey;

public class TigKeyServiceImpl extends TigKeyServiceGrpc.TigKeyServiceImplBase {
    private final static Logger logger = Logger.getLogger(TigKeyServiceImpl.class);

    @Override
    public void helloTigKey(Tig.HelloTigKeyRequest request, StreamObserver<Tig.HelloTigKeyReply> reply) {
        System.out.println(String.format("Hello %s", request.getRequest()));
        Tig.HelloTigKeyReply keyReply = Tig.HelloTigKeyReply.newBuilder().setRequest("Hello from Tig Key").build();
        reply.onNext(keyReply);
        reply.onCompleted();
    }

    @Override
    public void loginTigKey(Tig.LoginTigKeyRequest request, StreamObserver<Tig.TigKeySessionIdMessage> reply) {
        logger.info(String.format("Login username: %s", request.getUsername()));

        UsersDAO.authenticateUser(request.getUsername(), request.getPassword());
        String sessionId = SessionAuthenticator.createSession(request.getUsername());

        Tig.TigKeySessionIdMessage.Builder builder = Tig.TigKeySessionIdMessage.newBuilder().setSessionId(sessionId);

        reply.onNext(builder.build());
        reply.onCompleted();

    }
    @Override
    public void logoutTigKey(Tig.TigKeySessionIdMessage request, StreamObserver<Empty> responseObserver) {
        logger.info("Logout no Key Server");
        SessionAuthenticator.clearSession(request.getSessionId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void keyFileTigKey(Tig.KeyFileTigKeyRequest request, StreamObserver<Tig.KeyFileTigKeyReply> reply) {
        logger.info(String.format("file with name: %s of owner: %s", request.getFilename(), request.getOwner()));
    }

    @Override
    public void canEditTigKey(Tig.KeyFileTigKeyRequest request, StreamObserver<Tig.CanEditTigKeyReply> reply) {
        logger.info(String.format("filename: %s, owner: %s, session id: %s", request.getFilename(), request.getOwner(), request.getSessionId()));

        String username = SessionAuthenticator.authenticateSession(request.getSessionId().toString()).getUsername();
        AuthenticationDAO.authenticateFileAccess(username, request.getFilename(), request.getOwner(), 1);

        SecretKey key = EncryptionUtils.generateAESKey();

    }

    @Override
    public void listFileTigKey(Tig.TigKeySessionIdMessage request, StreamObserver<Tig.ListFilesReply> reply) {

    }

}
