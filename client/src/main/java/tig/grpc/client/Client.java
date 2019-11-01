package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Arrays;

public class Client {

    private String username;
    private String password;
    private TigServiceGrpc.TigServiceBlockingStub stub;
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public TigServiceGrpc.TigServiceBlockingStub getStub() {
        return stub;
    }


    public Client(TigServiceGrpc.TigServiceBlockingStub stub, String username, String password) {
        this.stub = stub;
        this.username = username;
        this.password = password;
    }


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
