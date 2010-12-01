package branch.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author qsh2
 *
 * BranchServerThread for the distributed banking system.
 * This is the main thread in the branch server environment.
 * It creates a ServerSocket that accepts connection request
 * from any client.
 * When a connection is accepted, it reads the message,
 * prepares the Message object and puts it in the MsgQueue.
 * Later on the MsgProcessingThread is going to pick it up from the thread
 * and process it.
 */


public class BranchServerThread extends Thread {
	private ServerProperties properties_;
	private MsgQueue messages_;
	private ServerSocket serverSocket_ = null;
		
	public BranchServerThread(ServerProperties properties) {
		properties_ = properties;
		messages_ = new MsgQueue();
		serverSocket_ = null;
	}
	
	public void run() {
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
			serverSocket_ = new ServerSocket(properties_.getPort());
		} catch (IOException e){
			System.err.println(
					"Coult not listen to port: " + properties_.getPort());
			System.exit(1);
		}

		System.out.println(properties_.print());

		// Initiate the TransactionThread which will wait till a message 
		// in the messageQueue appears.
		MsgProcessingThread tThread = new MsgProcessingThread(messages_, properties_);
		tThread.start();
		
		// Server starts listening.
		while (true) {
			try {
				Socket clientSocket = serverSocket_.accept();

				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				String msg = in.readLine();
				System.out.print(properties_.getServerName() + " : ");
				Message requestMessage = Message.parseString(msg);

				// Branch server does not expect response type messages.
				if (requestMessage.getType() == Message.MsgType.RESP) {
					System.err.println(properties_.getServerName()+ ": Received response: " + msg);
					continue;
				}

				messages_.addMsg(requestMessage);
			} catch (IOException e) {
				System.err.println("Coult not accept connection.");
				System.exit(1);
			}
		}
	}

	public ServerProperties getProperties() {
		return properties_;
	}
}
