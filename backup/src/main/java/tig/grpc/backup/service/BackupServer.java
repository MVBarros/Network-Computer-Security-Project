package tig.grpc.backup.service;

import org.apache.log4j.Logger;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;

import java.io.File;

import tig.grpc.backup.api.BackupServerImpl;
import tig.grpc.contract.TigBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.utils.db.PostgreSQLJDBC;
import tig.utils.interceptor.ExceptionHandler;


public class BackupServer {

    private static final Logger logger = Logger.getLogger(BackupServer.class);

    public static void main(String[] args) throws Exception{
        System.out.println(BackupServer.class.getSimpleName());

        // check arguments
        if (args.length < 6) {
            System.err.println("Argument(s) missing!");
            System.err.printf("<Usage> java %s port dbname certChainFile privateKeyFile trustCertCollection keyServerURL%n", BackupServer.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final BindableService impl = new BackupServerImpl();


        //Initialize Postgres Connection
        PostgreSQLJDBC.setDbName(args[1]);
        PostgreSQLJDBC.getInstance();

        //Key Server Public Key Certificate
        File certChainFile = new File(args[2]);

        //Key Server Private Key
        File privateKeyFile = new File(args[3]);

        //main Server Private Key
        File trustCertCollection = new File(args[4]);

        //Ssl Context requiring server authentication
        SslContext context  = GrpcSslContexts.forServer(certChainFile, privateKeyFile)
                .trustManager(trustCertCollection)
                .clientAuth(ClientAuth.REQUIRE)
                .build();

        ManagedChannel keyChannel = NettyChannelBuilder.forTarget(args[5])
                .sslContext(context)
                .build();
        TigKeyServiceGrpc.TigKeyServiceBlockingStub keyStub = TigKeyServiceGrpc.newBlockingStub(keyChannel);

        BackupServerImpl.keyStub = keyStub;

        final Server server = NettyServerBuilder
                .forPort(port)
                .sslContext(context)
                .intercept(new ExceptionHandler())
                .addService(impl)
                .build();

        server.start();

        logger.info("Backup Server started with authentication required");

        //So we can use CTRL-C when testing
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutdown Signal: Shutting down server");
                server.shutdownNow();
            }
        });

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();

    }

}
