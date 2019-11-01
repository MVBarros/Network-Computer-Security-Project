package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class Operations {

    public static void registerClient(Client client) {
        try {
            System.out.println("Register Client");
            System.out.println(client.getStub().register(Tig.LoginRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build()).getCode().toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Error registering user");
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
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

    //TODO If server sent the file percentage could have completion percentage show up on the terminal
    public static void downloadFile(Client client, String fileId, String filename) {
        try {
            System.out.println("Download File");
            Iterator<Tig.FileChunk> iterator = client.getStub().downloadFile(Tig.FileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFileName(fileId).build());

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

            for (; iterator.hasNext(); ) {
                Tig.FileChunk chunk = iterator.next();
                byte[] fileBytes = chunk.getContent().toByteArray();
                out.write(fileBytes);
            }
            out.flush();
            out.close();
        } catch (StatusRuntimeException e) {
            System.out.println("Error downloading file");
            System.out.println(e.getStatus().getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
