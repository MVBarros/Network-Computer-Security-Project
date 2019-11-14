package tig.grpc.backup.api;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import com.google.protobuf.Empty;

public class BackupServerImpl extends TigBackupServiceGrpc.TigBackupServiceImplBase {
    private final static Logger logger = Logger.getLogger(BackupServerImpl.class);

    @Override
    public void listBackupFiles (Tig.ListBackupFilesRequest request, StreamObserver<Tig.ListFilesReply> reply) {

    }

    @Override
    public void recoverFile (Tig.RecoverFileRequest request, StreamObserver<Tig.FileChunkDownload> reply) {

    }

    @Override
    public StreamObserver<Tig.BackupFileUpload> insertFileBackup (StreamObserver<Empty> reply) {
        return null;
    }

}
