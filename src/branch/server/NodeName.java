package branch.server;

import java.util.Vector;

/**
 * 
 * @author qsh2
 * 
 * NodeName class.
 * Gives a static interface for getting different components out of a node/server name.
 * Separates out the messy string parsing.
 * 
 */
public class NodeName {	
	
	enum Type {
		GUI,
		BRANCHSERVER,
		BRANCHSERVICE,
		FAILUREDETECTIONSERVER,
		FAILUREDETECTIONSERVICE
	};
	
	public static String getService(String node) {
		if(NodeName.isGui(node))
			return getServiceForGui(node);
		else if(NodeName.isBranchServer(node))
			return getServiceForServer(node);
		return node;
	}
	
	public static String getGui(String node) {
		if(NodeName.isGui(node))
			return node;
		else if(NodeName.isBranchServer(node))
			return getGuiForService(node);
		
		System.err.println("getGUI called for non-server / non-gui node.");
		return node;
	}
	
	public static boolean isGui(String node) {
		return node.startsWith("G");
	}
	
	public static boolean isBranchServer(String node) {
		return node.startsWith("S") && node.matches("S\\d{2}_M\\d{2}");
	}
	
	public static boolean isBranchService(String node) {
		if(node.startsWith("S") && node.matches("S\\d{2}"))
			return true;
		return false;
	}
	
	public static boolean isFailureDetectionServer(String server) {
		return server.startsWith("F");
	}

	public static boolean isFailureDetectionService(String service) {
		return service.startsWith("F") && service.charAt(3)=='_';
	}

	public static Type getType(String nodeName) {
		if(isGui(nodeName))
			return Type.GUI;
		if(isBranchServer(nodeName))
			return Type.BRANCHSERVER;
		if(isBranchService(nodeName))
			return Type.BRANCHSERVICE;
		if(isFailureDetectionServer(nodeName))
			return Type.FAILUREDETECTIONSERVER;
		if(isFailureDetectionService(nodeName))
			return Type.FAILUREDETECTIONSERVICE;
		
		return null;
	}
	
	public static String getFailureDetectionService(String failureDetServer) {
		if(!failureDetServer.startsWith("F") || failureDetServer.charAt(3)!='_' || failureDetServer.length()!=7)
			return null;
		return failureDetServer.substring(0, 3);
	}
	
	public static String getGuiForService(String service) {
		return "G" + service.substring(1,3);
	}
	
	public static String getGuiForServer(String server) {
		return "G" + server.substring(1,3);
	}
	
	public static String getServiceForGui(String gui) {
		return "S" + gui.substring(1);
	}
	
	public static String getServiceForServer(String server) {
		return server.substring(0,3);
	}
	
	public static String getMachineForServer(String server) {
		return server.substring(4);
	}

	public static String getFailureDetectionServer(
			Vector<String> serversForMachine) {
		for (String server : serversForMachine) {
			if(isFailureDetectionServer(server))
				return server;
		}
		return null;
	}
	
}