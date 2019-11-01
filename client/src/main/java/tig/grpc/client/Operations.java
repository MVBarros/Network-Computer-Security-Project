package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;

public class Operations {

    public static void registerClient(Client client) {
        try {
            System.out.println("Register Client");
            System.out.println(client.getStub().register(Tig.LoginRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build()).getCode().toString());
        }catch (StatusRuntimeException e) {
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
        }catch (StatusRuntimeException e) {
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
        }catch (StatusRuntimeException e) {
            System.out.println("Error logging out");
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

}