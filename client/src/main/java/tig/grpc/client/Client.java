package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {

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

        // Channel is the abstraction to connect to a service endpoint
        // Let us use plaintext communication because we do not have certificates
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        // It is up to the client to determine whether to block the call
        // Here we create a blocking stub, but an async stub,
        // or an async stub with Future are always possible.
        TigServiceGrpc.TigServiceBlockingStub stub = TigServiceGrpc.newBlockingStub(channel);

        try {
            // TODO testar deleteFile!
            System.out.println("Register User");
            System.out.println(stub.register(Tig.LoginRequest.newBuilder().setUsername("micaszocas8").setPassword("1234").build()).getCode().toString());
            System.out.println("Login User");
            String sessionId = stub.login(Tig.LoginRequest.newBuilder().setUsername("micaszocas8").setPassword("1234").build()).getSessionId();
            System.out.println(sessionId);
            System.out.println("Logout User");
            stub.logout(Tig.SessionRequest.newBuilder().setSessionId(sessionId).build());
            System.out.println("Login User again");
            sessionId = stub.login(Tig.LoginRequest.newBuilder().setUsername("micaszocas8").setPassword("1234").build()).getSessionId();
            System.out.println(sessionId);
            System.out.println("Logout User again");
            stub.logout(Tig.SessionRequest.newBuilder().setSessionId(sessionId).build());

        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getCode());
            System.out.println(e.getStatus().getDescription());
        }
        // A Channel should be shutdown before stopping the process.
        channel.shutdownNow();
    }
}
