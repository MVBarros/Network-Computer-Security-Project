package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Operations {

    public static void registerClient(Client client) {
        try {
            System.out.println("Register Client");
            System.out.println(client.getStub().register(Tig.LoginRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build()).getCode().toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Error registering user");
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void loginClient(Client client) {
        try {
            System.out.println("Login Client");
            client.setSessionId(client.getStub().login(Tig.LoginRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build()).getSessionId().toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Error logging in");
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    public static void logoutClient(Client client) {
        try {
            System.out.println("logout Client");
            client.getStub().logout(Tig.SessionRequest.newBuilder().setSessionId(client.getSessionId()).build());
            client.setSessionId(null);
        } catch (StatusRuntimeException e) {
            System.out.println("Error logging out");
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    //TODO If server sent the file percentage could have completion percentage show up on the terminal
    public static void downloadFile(Client client, String fileId, String filename) {
        try {
            System.out.println("Download File");
            client.getStub().downloadFile(Tig.FileRequest.newBuilder()
                            .setSessionId(client.getSessionId())
                            .setFileName(fileId).build(),
                    new StreamObserver<Tig.FileChunk>() {

                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

                        @Override
                        public void onNext(Tig.FileChunk fileChunk) {
                            synchronized (this) {
                                try {
                                    out.write(fileChunk.getContent().toByteArray());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCompleted() {
                            try {
                                out.flush();
                                out.close();
                                System.out.println("File successfully downloaded");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            try {
                                out.flush();
                                out.close();
                                System.out.println("Unknown error downloading file, try again");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (StatusRuntimeException e) {
            System.out.println("Error downloading file");
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
