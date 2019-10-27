package tig.grpc.client;

/* these are generated by the hello-world-server contract */
import sirs.grpc.contract.api.HelloWorld;
import sirs.grpc.contract.api.HelloWorldServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {

	public static void main(String[] args) throws Exception {
		System.out.println(Client.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

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
		HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stub = HelloWorldServiceGrpc.newBlockingStub(channel);
		HelloWorld.HelloRequest request = HelloWorld.HelloRequest.newBuilder().setName("friend").build();

		// Finally, make the call using the stub
		HelloWorld.HelloResponse response = stub.greeting(request);

		// HelloResponse has auto-generated toString method that shows its contents
		System.out.println(response);

		// A Channel should be shutdown before stopping the process.
		channel.shutdownNow();
	}

}
