package branch.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 
 * @author qsh2
 *
 * BranchServer for the distributed banking system.
 * This is the main class in the branch server environment.
 * It creates a ServerSocket that accepts connection request
 * from any client.
 * When a connection is accepted, it reads the message,
 * prepares the Message object and puts it in the MsgQueue.
 * Later on the MsgProcessingThread is going to pick it up from the thread
 * and process it.
 * 
 *  Usage:
 *  To run this program you have to give
 *  -id $branch-id -topology $topology-file-location -servers $server-file-location
 */


public class BranchServer {
	private static NodeProperties properties_;
	public static NodeProperties getProperties() {
		return properties_;
	}
	
	public static void main(String[] args) {		
		ServerSocket serverSocket = null;
		MsgQueue messages = new MsgQueue();

		// Parse the flags to get the arguments.
		properties_ = null;
		try {
			properties_ = new NodeProperties(args, false);
		} catch (NodeProperties.NodePropertiesException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		NetworkWrapper.setProperties(properties_);
		
		String myIp = properties_.getIp();
		if (!myIp.equals("localhost") && !myIp.equals("127.0.0.1")) {
			InetAddress inet = null;
			try {
				inet = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				System.err.println("Could not get inet-address for machine.");
				System.exit(1);
			}

			if (!inet.getHostAddress().equals(myIp)) {
				System.err.println("Server is supposed to start from: " + myIp);
				System.exit(1);
			}
		}

		// Check that a valid port was provided.
		if (properties_.getPort() < 0) {
			System.err.println("port not assigned.");
			System.exit(1);
		}

		// Create a ServerSocket for the given port.
		try {
			serverSocket = new ServerSocket(properties_.getPort());
		} catch (IOException e){
			System.err.println(
					"Coult not listen to port: " + properties_.getPort());
			System.exit(1);
		}

		System.out.println(properties_.print());

		// Initiate the TransactionThread which will wait till a message 
		// in the messageQueue appears.
		MsgProcessingThread tThread = new MsgProcessingThread(messages);
		tThread.start();

		/*// registering with Oracle.
		ArrayList<String> neighbors = new ArrayList<String>();
		Collections.copy(neighbors, properties_.getTopology().getOutNeighbors());
		new Register(properties_.getNode(), properties_.getGroupId(), neighbors);
		*/
		
		// Server starts listening.
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();

				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				String msg = in.readLine();
				Message requestMessage = Message.parseString(msg);

				// Branch server does not expect response type messages.
				if (requestMessage.getType() == Message.MsgType.RESP) {
					System.err.println("Received response: " + msg);
					continue;
				}

				messages.addMsg(requestMessage);
			} catch (IOException e) {
				System.err.println("Coult not accept connection.");
				System.exit(1);
			}
		}
	}
}
