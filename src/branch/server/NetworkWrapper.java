package branch.server;
/**
 * Creates a network abstraction for the servers.
 * It checks the Topology to verify whether one is allowed the send the message or not.
 * It also consults the servers.txt file (provided by the servers cmd line argument)
 * to get the host:port and eventually the socket of the destination.
 * @author qsh2
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkWrapper {
	private ServerProperties properties_ = null;
	private Topology tpl_ = null;
	
	public NetworkWrapper(ServerProperties properties) {
		properties_ = properties;
		tpl_ = properties.getTopology();
	}
	
	public ServerProperties getServerProperties() {
		return properties_;
	}
	
	public boolean sendToService(String msg, String service) {
		if (!properties_.isServiceReachable(service)) {
			System.err.println("Not reachable : " + service.toString());
			return false;
		}
		
		View destView = properties_.getView(service);
		return send(msg, destView.getHead());
	}
	
	public boolean queryToServiceTail(String msg, String service) {
		if (!properties_.isServiceReachable(service)) {
			System.err.println("Not reachable : " + service.toString());
			return false;
		}
		
		View destView = properties_.getView(service);
		return send(msg, destView.getTail());
	}
	
	public boolean sendToServer(String msg, String server) {
		// This method is kept for backward compatibility.
		return send(msg, server);
	}
	
	public boolean sendToGui(String msg) {
		String destNode = NodeName.getGui(properties_.getServiceId());
		return send(msg, destNode);	
	}
	
	public synchronized boolean send(String msg, String destServer) {
		if (!tpl_.isServerReachable(properties_.getServerName(), destServer)) {
			System.err.println("Not reachable : " + destServer.toString());
			return false;
		}
		
		Socket destSocket = getSocketForServer(destServer);
		if (destSocket == null) {
			System.err.println("Could not connect to : " + destServer.toString());
		}
		
		return send(msg, destSocket);
	}

	// Sends 'msg' to 'destSocket'
	private boolean send(String msg, Socket destSocket) {
		if (destSocket == null) {
			return false;
		}
		boolean success = false;

		try {
			PrintWriter out = new PrintWriter(destSocket.getOutputStream(), true);
			out.println(msg);
			out.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} finally {
			try {
				destSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		

		return success;
	}

	private Socket getSocketForServer(String server) {	
		Socket s = null;
		
		try {
			NodeLocations.Location serverLocation =
				properties_.getServerLocations().getLocationForNode(server);
			
			if (serverLocation == null) {
				return null;
			}

			s = new Socket(serverLocation.getIp(), serverLocation.getPort());
		} catch (UnknownHostException e) {
			System.err.println("Could not create socket for " + server.toString());
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not create socket for " + server.toString());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return s;
	}
}
