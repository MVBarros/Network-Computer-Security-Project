package tig.grpc.backup.api;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.backup.dao.FileDAO;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import com.google.protobuf.Empty;

import java.util.Arrays;

public class BackupServerImpl extends TigBackupServiceGrpc.TigBackupServiceImplBase {
    private final static Logger logger = Logger.getLogger(BackupServerImpl.class);

    @Override
    public void listBackupFiles (Tig.ListBackupFilesRequest request, StreamObserver<Tig.ListFilesReply> reply) {

    }

    @Override
    public void recoverFile (Tig.RecoverFileRequest request, StreamObserver<Tig.FileChunkDownload> reply) {
        logger.info(String.format("Recover file: %s", request.getFilename()));

        String filename = request.getFilename();
        String time_created = request.getTCreated();

        byte[] file = FileDAO.getFileContent(filename, time_created);

        int sequence = 0;
        //Send file 1MB chunk at a time
        for (int i = 0; i < file.length; i += 1024 * 1024, sequence++) {
            int chunkSize = Math.min(1024 * 1024, file.length - i);
            Tig.FileChunkDownload.Builder builder = Tig.FileChunkDownload.newBuilder();
            builder.setContent(ByteString.copyFrom(Arrays.copyOfRange(file, i, i + chunkSize)));
            builder.setSequence(sequence);
            reply.onNext(builder.build());
        }
        reply.onCompleted();
    }

    @Override
    public StreamObserver<Tig.BackupFileUpload> insertFileBackup (StreamObserver<Empty> reply) {
        return null;
    }

}
