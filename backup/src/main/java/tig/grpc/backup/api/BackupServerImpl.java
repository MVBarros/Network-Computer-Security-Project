package tig.grpc.backup.api;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import tig.grpc.backup.dao.FileDAO;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import com.google.protobuf.Empty;
import java.util.List;

import java.util.Arrays;

public class BackupServerImpl extends TigBackupServiceGrpc.TigBackupServiceImplBase {
    private final static Logger logger = Logger.getLogger(BackupServerImpl.class);

    public static TigBackupServiceGrpc.TigBackupServiceBlockingStub keystub;

    @Override
    public void listBackupFiles (Tig.ListBackupFilesRequest request, StreamObserver<Tig.ListFilesReply> reply) {
        String username = SessionAuthenticator.authenticateSession(request.getSessionId()).getUsername();

        logger.info("List files that can be recovered " + username);
        List<String> files = FileDAO.listFiles(username);
        Tig.ListFilesReply.Builder builder = Tig.ListFilesReply.newBuilder();
        builder.addAllFileInfo(files);
        reply.onNext(builder.build());
        reply.onCompleted();
    }

    @Override
    public void recoverFile (Tig.RecoverFileRequest request, StreamObserver<Tig.FileChunkDownload> reply) {
        logger.info(String.format("Recover file: %s", request.getFilename()));

        String filename = request.getFilename();
        String owner = SessionAuthenticator.authenticateSession(request.getSessionId()).getUsername();
        String time_created = request.getTCreated();

        byte[] file = FileDAO.getFileContent(filename, owner, time_created);

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
    public StreamObserver<Tig.BackupFileUpload> insertFileBackup (StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Tig.BackupFileUpload> () {

            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[]{});
            private String filename;
            private String owner;
            private String t_created;
            private final Object lock = new Object();

            @Override
            public void onNext(Tig.BackupFileUpload backupFileUpload) {
                //Synchronize onNext calls by sequence
                synchronized (lock) {
                    while (counter != backupFileUpload.getSequence()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    //Renew Lease
                    if (counter == 0) {
                        filename = backupFileUpload.getFileName();
                        owner = backupFileUpload.getFileowner();
                        t_created = backupFileUpload.getTCreated();
                    }
                    file = file.concat(backupFileUpload.getContent());
                    counter++;
                    lock.notify();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                FileDAO.uploadFile(filename, owner, t_created, file.toByteArray());
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }

}
