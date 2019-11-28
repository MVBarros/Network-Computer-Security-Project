package tig.grpc.keys.api;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.keys.dao.UsersDAO;
import tig.grpc.keys.session.SessionAuthenticator;

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
    public void loginTigKey(Tig.LoginTigKeyRequest request, StreamObserver<Tig.LoginTigKeyReply> reply) {
        logger.info(String.format("Login username: %s", request.getUsername()));

        UsersDAO.authenticateUser(request.getUsername(), request.getPassword());
        String sessionId = SessionAuthenticator.createSession(request.getUsername());

        Tig.LoginTigKeyReply.Builder builder = Tig.LoginTigKeyReply.newBuilder().setSessionId(sessionId);

        reply.onNext(builder.build());
        reply.onCompleted();

    }

    @Override
    public void keyFileTigKey(Tig.KeyFileTigKeyRequest request, StreamObserver<Tig.KeyFileTigKeyReply> reply) {
        logger.info(String.format("file with name: %s of owner: %s", request.getFilename(), request.getOwner()));
    }

    @Override
    public void canSaveTigKey(Tig.CanEditTigKeyRequest request, StreamObserver<Tig.CanEditTigKeyReply> reply) {
        logger.info(String.format("session id: %s", request.getSessionId()));
    }

}
