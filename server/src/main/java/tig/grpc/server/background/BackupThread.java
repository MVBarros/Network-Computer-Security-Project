package tig.grpc.server.background;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class BackupThread implements Runnable{
    private byte[] content;
    private String fileId;
    private String sessionId;
    private String t_created;
    public static TigBackupServiceGrpc.TigBackupServiceStub backupStub;


    public BackupThread(byte[] content, String fileId, String sessionId, String t_created) {
        this.content = content;
        this.fileId = fileId;
        this.sessionId = sessionId;
        this.t_created = t_created;
    }

    public void run() {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) {
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error uploading file for backup");
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File Backup uploaded successfully");
                finishLatch.countDown();
            }
        };

        //Send file one megabyte at a time
        byte[] data = new byte[1024 * 1024];

        StreamObserver<Tig.BackupFileUpload> requestObserver = backupStub.insertFileBackup(responseObserver);

        try (BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(content))) {
            int numRead;
            //Send file chunks to server
            while ((numRead = in.read(data)) >= 0) {
                Tig.BackupFileUpload.Builder fileChunk = Tig.BackupFileUpload.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(Arrays.copyOfRange(data, 0, numRead)));
                fileChunk.setFileId(fileId);
                fileChunk.setSessionId(sessionId);
                fileChunk.setSequence(sequence);
                fileChunk.setTCreated(t_created);
                requestObserver.onNext(fileChunk.build());
                sequence++;
            }

            requestObserver.onCompleted();

            //Wait for server to finish saving file to Database
            finishLatch.await();

        } catch (InterruptedException | IOException e) {
            //Should never happen
            e.printStackTrace();
        }
    }
}
