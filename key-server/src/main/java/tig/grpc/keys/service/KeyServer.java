package tig.grpc.keys.service;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;
import tig.grpc.keys.api.TigKeyServiceImpl;
import tig.utils.db.PostgreSQLJDBC;
import tig.utils.interceptor.ExceptionHandler;
import tig.utils.keys.KeyFileLoader;

import javax.net.ssl.SSLContext;
import java.io.File;

public class KeyServer {

    private static final Logger logger = Logger.getLogger(KeyServer.class);

    public static void main(String[] args) throws Exception{
        System.out.println(KeyServer.class.getSimpleName());

        // check arguments
        if (args.length < 5) {
            System.err.println("Argument(s) missing!");
            System.err.printf("<Usage> java %s port dbname certChainFile privateKeyFile trustCertCollection%n", KeyServer.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final BindableService impl = new TigKeyServiceImpl();


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


        final Server server = NettyServerBuilder
                .forPort(port)
                .sslContext(context)
                .intercept(new ExceptionHandler())
                .addService(impl)
                .build();

        server.start();

        logger.info("Key Server started with authentication required");

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
