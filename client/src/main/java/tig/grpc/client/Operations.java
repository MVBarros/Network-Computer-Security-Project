package tig.grpc.client;

import com.google.protobuf.CodedOutputStream;
import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

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

    public static void listFiles(Client client) {
        // TODO nao ha excepcao?
        System.out.println("List all Files");
        Tig.ListFilesReply reply = client.getStub().listFiles(Tig.SessionRequest.newBuilder()
                .setSessionId(client.getSessionId()).build());
        System.out.println(reply.toString());

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
