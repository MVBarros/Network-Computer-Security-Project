package tig.grpc.client;

import com.google.protobuf.ByteString;
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
            client.getStub().register(Tig.LoginRequest.newBuilder()
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
            client.setSessionId(client.getStub().login(Tig.LoginRequest.newBuilder()
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

    public static void uploadFile(Client client, String filePath, String filename) {
        System.out.println(String.format("Upload new file with filename %s", filename));

        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;

        StreamObserver<Tig.StatusReply> responseObserver = new StreamObserver<Tig.StatusReply>() {
            @Override
            public void onNext(Tig.StatusReply statusReply) {
            }

            @Override
            public void onError(Throwable throwable) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        //Send file one megabyte at a time
        byte[] data = new byte[1024 * 1024];

        StreamObserver<Tig.FileChunk> requestObserver = client.getAsyncStub().uploadFile(responseObserver);

        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
            int numRead;
            //Send file chunks to server
            while ((numRead =in.read(data)) >= 0) {
                Tig.FileChunk.Builder fileChunk = Tig.FileChunk.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(Arrays.copyOfRange(data, 0, numRead)));
                fileChunk.setFileName(filename);
                fileChunk.setSessionId(client.getSessionId());
                fileChunk.setSequence(sequence);
                requestObserver.onNext(fileChunk.build());

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    //This should never happen
                    return;
                }
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
        } catch (StatusRuntimeException e) {
            System.out.print("Error uploading file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }

    }

    public static void editFile(Client client, String fileID, String filename) {
        System.out.println(String.format("Edit file with fileId %s e filename %s", fileID, filename));

        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;

        StreamObserver<Tig.StatusReply> responseObserver = new StreamObserver<Tig.StatusReply>() {
            @Override
            public void onNext(Tig.StatusReply statusReply) {
                System.out.println(statusReply.getCode().toString());
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        //Send file one megabyte at a time
        byte[] data = new byte[1024 * 1024];

        StreamObserver<Tig.FileChunk> requestObserver = client.getAsyncStub().editFile(responseObserver);

        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

            //Send file chunks to server
            while ((in.read(data)) != -1) {
                Tig.FileChunk.Builder fileChunk = Tig.FileChunk.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(data));
                fileChunk.setFileName(fileID);
                fileChunk.setSessionId(client.getSessionId());
                fileChunk.setSequence(sequence);
                requestObserver.onNext(fileChunk.build());

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    //This should never happen
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
        } catch (StatusRuntimeException e) {
            System.out.print("Error editing file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }

    }

    public static void deleteFile(Client client, String fileId) {
        try {
            System.out.println(String.format("Delete File with fileid %s ", fileId));
            client.getStub().deleteFile(Tig.FileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFileName(fileId).build());
            System.out.println(String.format("File %s Successfully deleted", fileId));
        } catch (StatusRuntimeException e) {
            System.out.print("Error deleting file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void setAccessControl(Client client, String fileid, Tig.OperationEnum permissions) {
        try {
            System.out.println(String.format("Set access control File fileid %s with PUBLIC = %b ", fileid, permissions));
            client.getStub().accessControlFile(Tig.OperationRequest.newBuilder()
                    .setFileName(fileid)
                    .setSessionId(client.getSessionId())
                    .setOperation(permissions).build());
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


    public static void downloadFile(Client client, String fileId, String filename) {
        try {
            System.out.println(String.format("Download File with fileid %s into file %s", fileId, filename));
            Iterator<Tig.FileChunk> iterator = client.getStub().downloadFile(Tig.FileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFileName(fileId).build());

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

            //Write bytes to file
            while(iterator.hasNext()) {
                Tig.FileChunk chunk = iterator.next();
                System.out.println(Arrays.toString(chunk.getContent().toByteArray()));
                byte[] fileBytes = chunk.getContent().toByteArray();
                out.write(fileBytes);
            }

            out.close();
            System.out.println(String.format("File %s successfully written with contents of fileId %s",
                    fileId, filename));
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
