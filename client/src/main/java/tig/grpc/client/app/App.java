package tig.grpc.client.app;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import tig.grpc.client.Client;
import tig.grpc.client.Operations;
import tig.grpc.contract.TigServiceGrpc;

import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s host port%n", Client.class.getName());
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final String target = host + ":" + port;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        TigServiceGrpc.TigServiceBlockingStub stub = TigServiceGrpc.newBlockingStub(channel);

        //Always Safely terminate connection
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                System.out.println("Shutting down channel");
//                channel.shutdownNow();
//            }
//        });

        String username = System.console().readLine("Username:");
        String password = Arrays.toString(System.console().readPassword("Password:"));

        Client client = new Client(stub, username, password);


        // TODO testar deleteFile downloadFile!
        Operations.registerClient(client);
        Operations.loginClient(client);
        Operations.logoutClient(client);
        Operations.loginClient(client);
        Operations.logoutClient(client);

        // A Channel should be shutdown before stopping the process.
        channel.shutdownNow();
    }
}
