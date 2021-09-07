package pt.tecnico.reg;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;

public class RegMain {

	/** ZooKeeper host name. */
	private static String zooHost;

	/** ZooKeeper host port. */
	private static String zooPort;

	/** ZooKeeper path where information about the server will be published. */
	private static String path;

	/** Server host name. */
	private static String localHost;

	/** Server host port. */
	private static String localPort;

	/** ZooKeeper helper object. */
	private static ZKNaming zkNaming = null;

	public static void main(String[] args) throws IOException, InterruptedException, Exception {
		System.out.println(RegMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		zooHost = args[0];
		zooPort = args[1];
		localHost = args[2];
		localPort = args[3];
		path = args[4];

		// Check arguments.
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort host port path %n", Server.class.getName());
			return;
		}

		final BindableService impl = new RegImpl();
		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(Integer.parseInt(localPort)).addService(impl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		// Register on ZooKeeper.

		System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");

		zkNaming = new ZKNaming(zooHost, zooPort);


		System.out.println("Binding " + path + " to " + zooHost + ":" + zooPort + "...");

		zkNaming.rebind(path, localHost, localPort.toString());

		// Use hook to register a thread to be called on shutdown.

		Runtime.getRuntime().addShutdownHook(new Unbind());

		System.out.println("Server started and awaiting requests on port " + localPort);
		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

	// Unbind class unbinds replica from ZKNaming after interruption.

	static class Unbind extends Thread {
		public void run() {
			if (zkNaming != null) {
				try {
					System.out.println("Unbinding " + path + " from ZooKeeper...");
					zkNaming.unbind(path, localHost, localPort.toString());
				} catch (ZKNamingException e) {
					System.err.println("Could not close connection with ZooKeeper: " + e);
					return;
				}
			}
		}
	}
}
