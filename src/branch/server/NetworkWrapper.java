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

public abstract class NetworkWrapper {
	private static NodeProperties properties_ = null;	

	public static void setProperties(NodeProperties properties) {
		properties_ = properties;
	}
	
	public static boolean sendToService(String msg, String service) {
		String server = null;
		final Topology tpl = properties_.getTopology();
		if (!tpl.isReachable(service)) {
			System.err.println("Not reachable : " + service.toString());
			return false;
		}
		
		if(properties_.views_ == null || properties_.views_.get(service)== null) {
			System.err.println("View is empty. Service unavailable.");
			return false;
		}
		
		server = properties_.views_.get(service).getHead();
		return send(msg, server);
	}
	
	public static boolean queryToServiceTail(String msg, String service) {
		String server = null;
		final Topology tpl = properties_.getTopology();
		if (!tpl.isReachable(service)) {
			System.err.println("Not reachable : " + service.toString());
			return false;
		}
		if(properties_.views_ == null || properties_.views_.get(service)== null) {
			System.err.println("View is empty. Service unavailable.");
			return false;
		}
		
		server = properties_.views_.get(service).getTail();
		return send(msg, server);
	}
	
	public static boolean sendToServer(String msg, String server) {
		// This method is kept for backward compatibility.
		return send(msg, server);
	}
	
	public static boolean sendToGui(String msg) {
		String destNode = NodeName.getGUI(properties_.getGroupId());
		return send(msg, destNode);	
	}
	
	public static boolean send(String msg, String destNode) {
		final Topology tpl = properties_.getTopology();
		String destService = NodeName.getService(destNode);
		if (!tpl.isReachable(destService.toString())) {
			System.err.println("Not reachable : " + destNode.toString());
			return false;
		}
		
		Socket destSocket = getSocketForNode(destNode);
		if (destSocket == null) {
			System.err.println("Could not connect to : " + destNode.toString());
		}
		
		return send(msg, destSocket);
	}

	// Sends 'msg' to 'destSocket'
	private static boolean send(String msg, Socket destSocket) {
		if (destSocket == null) {
			return false;
		}
		boolean success = false;

		try {
			PrintWriter out = new PrintWriter(destSocket.getOutputStream(), true);
			out.println(msg);
			out.close();
			destSocket.close();
			success = true;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		

		return success;
	}

	private static Socket getSocketForNode(String node) {	
		Socket s = null;
		
		try {
			NodeLocations.Location serverLocation =
				properties_.getServerLocations().getLocationForNode(node);
			
			if (serverLocation == null) {
				return null;
			}

			s = new Socket(serverLocation.getIp(), serverLocation.getPort());
		} catch (UnknownHostException e) {
			System.err.println("Could not create socket for " + node.toString());
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not create socket for " + node.toString());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return s;
	}
}
