package tig.grpc.client;

import com.google.longrunning.Operation;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;

import java.io.*;
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
            System.out.println(String.format("logout Client %s ", client.getUsername() ));
            client.getStub().logout(Tig.SessionRequest.newBuilder().setSessionId(client.getSessionId()).build());
            client.setSessionId(null);
            System.out.println(String.format("User %s Successfully logged out", client.getUsername()));
        } catch (StatusRuntimeException e) {
            System.out.print("Error logging out: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void uploadFile(Client client, String filename) {
        System.out.println(String.format("Upload new file with filename %s", filename));

        final CountDownLatch finishLatch = new CountDownLatch(1);
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

        byte[] data = new byte[1024];

        StreamObserver<Tig.FileChunk> requestObserver = client.getAsyncStub().uploadFile(responseObserver);

        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

            while ((in.read(data)) != -1) {
                Tig.FileChunk.Builder fileChunk = Tig.FileChunk.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(data));
                fileChunk.setFileName(filename);
                requestObserver.onNext(fileChunk.build());
            }

            requestObserver.onCompleted();

            finishLatch.wait();

        } catch(FileNotFoundException e) {
            System.out.println(String.format("File with filename: %s not found.", filename));
        } catch(IOException e) {
            System.exit(1);
        } catch(InterruptedException e) {
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

        byte[] data = new byte[1024];

        StreamObserver<Tig.FileChunk> requestObserver = client.getAsyncStub().editFile(responseObserver);

        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

            while ((in.read(data)) != -1) {
                Tig.FileChunk.Builder fileChunk = Tig.FileChunk.newBuilder();
                fileChunk.setContent(ByteString.copyFrom(data));
                fileChunk.setFileName(fileID);
                requestObserver.onNext(fileChunk.build());
            }

            requestObserver.onCompleted();

            finishLatch.wait();

        } catch(FileNotFoundException e) {
            System.out.println(String.format("File with filename: %s not found.", filename));
        } catch(IOException e) {
            System.exit(1);
        } catch(InterruptedException e) {
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

    public static void setAccessControl (Client client, String fileid, Tig.OperationEnum permissions) {
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
            System.out.println(reply.toString());

        } catch (StatusRuntimeException e) {
            System.out.print("Error listing files: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
         }

        // sera melhor usar?
        // String s  = reply.writeTo(?);
        // String i  = reply.getFileNames(i);
    }



    //TODO If server sent the file percentage could have completion percentage show up on the terminal
    //TODO Maybe make it an asynchronous stub
    public static void downloadFile(Client client, String fileId, String filename) {
        try {
            System.out.println(String.format("Download File with fileid %s into file %s", fileId, filename));
            Iterator<Tig.FileChunk> iterator = client.getStub().downloadFile(Tig.FileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFileName(fileId).build());

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

            for (Tig.FileChunk chunk = iterator.next(); iterator.hasNext(); chunk = iterator.next() ) {
                byte[] fileBytes = chunk.getContent().toByteArray();
                out.write(fileBytes);
            }

            out.flush();
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
