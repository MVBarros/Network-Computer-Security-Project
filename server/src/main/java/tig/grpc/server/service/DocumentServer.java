package tig.grpc.server.service;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.log4j.Logger;
import tig.grpc.server.api.TigServiceImpl;
import tig.grpc.server.data.PostgreSQLJDBC;
import tig.grpc.server.interceptor.ExceptionHandler;
import tig.grpc.server.session.TokenCleanupThread;

public class DocumentServer {
    private static final Logger logger = Logger.getLogger(DocumentServer.class);

    public static void main(String[] args) throws Exception {
        System.out.println(DocumentServer.class.getSimpleName());

        // check arguments
        if (args.length < 3) {
            System.err.println("Argument(s) missing!");
            System.err.printf("<Usage> java %s port dbport dbpassword%n", DocumentServer.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final BindableService impl = new TigServiceImpl();

        //Initialize Postgres Connection
        PostgreSQLJDBC.setPort(Integer.parseInt(args[1]));
        PostgreSQLJDBC.setPassword(args[2]);
        PostgreSQLJDBC.getInstance();

        Server server = ServerBuilder
                .forPort(port)
                .intercept(new ExceptionHandler())
                .addService(impl)
                .build();

        server.start();

        logger.info("Server started");

        //So we can use CTRL-C when testing
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutdown Signal: Shutting down server");
                server.shutdownNow();
            }
        });

        //Cleanup hanging Session Tokens in the background
        new Thread(new TokenCleanupThread()).start();

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}
