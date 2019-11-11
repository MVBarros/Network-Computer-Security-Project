package tig.grpc.client.app;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import org.apache.commons.cli.*;
import tig.grpc.client.Client;
import tig.grpc.client.Operations;
import tig.grpc.client.options.OptionManager;
import tig.grpc.contract.TigServiceGrpc;

import java.io.File;
import java.util.Arrays;


public class App {
    //Client is static so we can logout in the Shutdown Hook
    private static Client client = null;

    public static void main(String[] args) throws Exception {

        Options options = OptionManager.createOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, Arrays.copyOfRange(args, 1,args.length));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        final String host = "localhost";//args[0];
        final int port = 8080;//Integer.parseInt(args[1]);
        final String target = host + ":" + port;

        final ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                                                        .sslContext(GrpcSslContexts.forClient().trustManager(new File(args[0])).build())
                                                        .build();

        TigServiceGrpc.TigServiceBlockingStub stub = TigServiceGrpc.newBlockingStub(channel);
        TigServiceGrpc.TigServiceStub asyncStub = TigServiceGrpc.newStub(channel);

        //Always Safely terminate connection and logout user
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (client != null && client.getSessionId() != null) {
                    Operations.logoutClient(client);
                }
                channel.shutdownNow();
            }
        });

        String username = System.console().readLine("Username:");
        String password = Arrays.toString(System.console().readPassword("Password:"));

        client = new Client(stub, asyncStub, username, password);

        OptionManager.executeOptions(cmd, client);
    }
}
