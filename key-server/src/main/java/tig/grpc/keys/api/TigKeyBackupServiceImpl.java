package tig.grpc.keys.api;

import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigKeyBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.keys.dao.FileDAO;
import tig.grpc.keys.session.SessionAuthenticator;
import tig.grpc.keys.session.UserToken;

public class TigKeyBackupServiceImpl extends TigKeyBackupServiceGrpc.TigKeyBackupServiceImplBase {
    @Override
    public void getUsernameForSession(Tig.TigKeySessionIdMessage request, StreamObserver<Tig.TigKeyUsernameMessage> responseObserver) {
        UserToken userToken = SessionAuthenticator.authenticateSession(request.getSessionId());
        String username = userToken.getUsername();

        Tig.TigKeyUsernameMessage reply = Tig.TigKeyUsernameMessage.newBuilder().setFileowner(username).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getFileForBackup(Tig.TigKeySessionIdMessage request, StreamObserver<Tig.TigKeyUsernameMessage> responseObserver) {
        String fileId = request.getFileId();
        String[] file = FileDAO.getFileName(fileId);

        Tig.TigKeyUsernameMessage reply = Tig.TigKeyUsernameMessage.newBuilder()
                .setFileowner(file[1])
                .setFilename(file[0])
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
