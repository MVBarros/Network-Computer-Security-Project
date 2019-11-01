package tig.grpc.server.service;

import io.grpc.Server;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import tig.grpc.server.api.TigServiceImpl;
import tig.grpc.server.data.PostgreSQLJDBC;

public class DocumentServer {

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
		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		//So we can use CTRL-C when testing
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down server");
				server.shutdownNow();
			}
		});
		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

}
