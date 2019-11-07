package tig.grpc.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class Operations {

    public static void registerClient(Client client) {
        try {
            System.out.println(String.format("Register Client %s", client.getUsername()));
            client.getStub().register(Tig.AccountRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build());
            System.out.println(String.format("User %s Successfully registered", client.getUsername()));
        } catch (StatusRuntimeException e) {
            System.out.print("Error registering user: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void loginClient(Client client) {
        try {
            System.out.println(String.format("Login Client %s", client.getUsername()
            ));
            client.setSessionId(client.getStub().login(Tig.AccountRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build()).getSessionId().toString());
            System.out.println(String.format("User %s Successfully logged in", client.getUsername()));
        } catch (StatusRuntimeException e) {
            System.out.print("Error logging in: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void logoutClient(Client client) {
        try {
            System.out.println(String.format("logout Client %s ", client.getUsername()));
            client.getStub().logout(Tig.SessionRequest.newBuilder().setSessionId(client.getSessionId()).build());
            client.setSessionId(null);
            System.out.println(String.format("User %s Successfully logged out", client.getUsername()));
        } catch (StatusRuntimeException e) {
            System.out.print("Error logging out: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void uploadFile(Client client, String filename, String filePath) {
        System.out.println(String.format("Upload new file with filename %s", filename));

        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;


        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) { }
            @Override
            public void onError(Throwable throwable) {
                System.out.print("Error uploading file: ");
                if(throwable instanceof StatusRuntimeException) {
                    System.out.println(((StatusRuntimeException) throwable).getStatus().getDescription());
                }
                else {
                    System.out.println("Unknown error");
                }
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File uploaded successfully");
                finishLatch.countDown();
            }
        };

        //Send file one megabyte at a time
        byte[] data = new byte[1024 * 1024];

        StreamObserver<Tig.FileChunkClientUpload> requestObserver = client.getAsyncStub().uploadFile(responseObserver);

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath))) {
            int numRead;
            //Send file chunks to server
            while ((numRead = in.read(data)) >= 0) {
                Tig.FileChunkClientUpload.Builder fileChunk = Tig.FileChunkClientUpload.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(Arrays.copyOfRange(data, 0, numRead)));
                fileChunk.setFileName(filename);
                fileChunk.setSessionId(client.getSessionId());
                fileChunk.setSequence(sequence);
                requestObserver.onNext(fileChunk.build());
                sequence++;
            }

            requestObserver.onCompleted();

            //Wait for server to finish saving file to Database
            finishLatch.await();

        } catch (FileNotFoundException e) {
            System.out.println(String.format("File with filename: %s not found.", filename));
        } catch (IOException | InterruptedException e) {
            //Should Never Happen
            System.exit(1);
        }
    }

    public static void editFile(Client client, String filename, String owner, String filepath) {
        System.out.println(String.format("Edit file with filename %s and owner %s", filename, owner));
        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) { }
            @Override
            public void onError(Throwable throwable) {
                System.out.print("Error editing file: ");
                if(throwable instanceof StatusRuntimeException) {
                    System.out.println(((StatusRuntimeException) throwable).getStatus().getDescription());
                }
                else {
                    System.out.println("Unknown error");
                }
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File edited successfully");
                finishLatch.countDown();
            }
        };

        //Send file one megabyte at a time
        byte[] data = new byte[1024 * 1024];
        StreamObserver<Tig.FileChunkClientEdit> requestObserver = client.getAsyncStub().editFile(responseObserver);
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filepath));

            //Send file chunks to server
            while ((in.read(data)) != -1) {
                Tig.FileChunkClientEdit.Builder fileChunk = Tig.FileChunkClientEdit.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(data));
                fileChunk.setFileName(filename);
                fileChunk.setOwner(owner);
                fileChunk.setSessionId(client.getSessionId());
                fileChunk.setSequence(sequence);
                requestObserver.onNext(fileChunk.build());
                if (finishLatch.getCount() == 0) {
                    // RPC errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return;
                }
                sequence++;
            }
            requestObserver.onCompleted();
            //Wait for server to finish saving file to Database
            finishLatch.wait();
        } catch (FileNotFoundException e) {
            System.out.println(String.format("File with filename: %s not found.", filename));
        } catch (IOException | InterruptedException e) {
            //Should Never Happen
            System.exit(1);
        }

    }

    public static void deleteFile(Client client, String filename) {
        try {
            System.out.println(String.format("Delete File %s ", filename));
            client.getStub().deleteFile(Tig.DeleteFileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFilename(filename).build());
            System.out.println(String.format("File %s Successfully deleted", filename));
        } catch (StatusRuntimeException e) {
            System.out.print("Error deleting file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void setAccessControl(Client client, String filename, String target, Tig.PermissionEnum permission) {
        try {
            System.out.println(String.format("Set access control File: %s to user: %s with %b ", filename, target, permission));
            client.getStub().accessControlFile(Tig.AccessControlRequest.newBuilder()
                    .setFileName(filename)
                    .setSessionId(client.getSessionId())
                    .setTarget(target)
                    .setPermission(permission).build());
        } catch (StatusRuntimeException e) {
            System.out.print("Error deleting file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void listFiles(Client client) {
        try {
            System.out.println("List all Files");
            Tig.ListFilesReply reply = client.getStub().listFiles(Tig.SessionRequest.newBuilder()
                    .setSessionId(client.getSessionId()).build());
            Object[] names = reply.getFileInfoList().toArray();
            for (Object name : names) {
                System.out.println(name.toString() + "\n");
            }

        } catch (StatusRuntimeException e) {
            System.out.print("Error listing files: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }


    public static void downloadFile(Client client, String filename, String owner, String filepath) {
        try {
            System.out.println(String.format("Download File with filename %s with owner %s. With file path: %s", filename, owner, filepath));
            Iterator<Tig.FileChunkDownload> iterator = client.getStub().downloadFile(Tig.FileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFileName(filename)
                    .setOwner(owner).build());

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filepath));

            //Write bytes to file
            while(iterator.hasNext()) {
                Tig.FileChunkDownload chunk = iterator.next();
                byte[] fileBytes = chunk.getContent().toByteArray();
                out.write(fileBytes);
            }

            out.close();
            System.out.println(String.format("File with filename %s with owner %s sucessfully downloaded into %s", filename, owner, filepath));
        } catch (StatusRuntimeException e) {
            System.out.print("Error downloading file: ");
            System.out.println(e.getStatus().getDescription());
        } catch (IOException e) {
            //Should never happen
            System.out.println("Error writing to file");
            System.out.println(String.format("Cause: %s", e.getMessage()));
            System.out.println("Aborting download");

        }
    }
}
