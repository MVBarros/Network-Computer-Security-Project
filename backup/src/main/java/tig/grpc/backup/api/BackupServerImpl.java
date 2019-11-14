package tig.grpc.backup.api;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;

public class BackupServerImpl extends TigBackupServiceGrpc.TigBackupServiceImplBase {
    private final static Logger logger = Logger.getLogger(BackupServerImpl.class);

    @Override
    public void helloTigBackup(Tig.HelloTigBackupRequest request, StreamObserver<Tig.HelloTigBackupReply> reply) {
        System.out.println(String.format("Hello %s", request.getRequest()));
        Tig.HelloTigBackupReply backupReply = Tig.HelloTigBackupReply.newBuilder().setRequest("Hello from Tig Backup").build();
        reply.onNext(backupReply);
        reply.onCompleted();
    }


}
