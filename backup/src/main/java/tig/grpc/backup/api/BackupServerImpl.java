package tig.grpc.backup.api;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.backup.dao.FileDAO;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import com.google.protobuf.Empty;

import java.util.List;

public class BackupServerImpl extends TigBackupServiceGrpc.TigBackupServiceImplBase {
    private final static Logger logger = Logger.getLogger(BackupServerImpl.class);

    @Override
    public void listBackupFiles (Tig.ListBackupFilesRequest request, StreamObserver<Tig.ListFilesReply> reply) {
        logger.info("List files that can be recovered " + request.getFileowner());
        List<String> files = FileDAO.listFiles(request.getFileowner());
        Tig.ListFilesReply.Builder builder = Tig.ListFilesReply.newBuilder();
        builder.addAllFileInfo(files);
        reply.onNext(builder.build());
        reply.onCompleted();
    }

    @Override
    public void recoverFile (Tig.RecoverFileRequest request, StreamObserver<Tig.FileChunkDownload> reply) {

    }

    @Override
    public StreamObserver<Tig.BackupFileUpload> insertFileBackup (StreamObserver<Empty> reply) {
        return null;
    }

}
