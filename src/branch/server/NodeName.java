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
	
	public enum Type {
		GUI,
		BRANCHSERVER,
		BRANCHSERVICE,
		FAILUREDETECTIONSERVER,
		FAILUREDETECTIONSERVICE, 
		SENSOR
	};
	
	public static String getService(String node) {
		Type type = getType(node);
		switch (type) {
		case GUI:
			return getServiceForGui(node);
		case BRANCHSERVER:
			return getServiceForServer(node);
		case BRANCHSERVICE:
			return node;
		case FAILUREDETECTIONSERVER:
			return getFailureDetectionService(node);
		case FAILUREDETECTIONSERVICE:
			return node;
		case SENSOR:
			return getServiceForServer(node);
		}
		return null;
	}
	
	public static String getGui(String node) {
		if(NodeName.isGui(node))
			return node;
		else if(NodeName.isBranchServer(node))
			return getGuiForServer(node);
		else if(NodeName.isBranchService(node))
			return getGuiForService(node);
		
		System.err.println("getGUI called for non-server / non-gui node.");
		return null;
	}
	
	public static boolean isGui(String node) {
		return node.startsWith("G");
	}
	
	public static boolean isBranchServer(String node) {
		return node.matches("\\d{2}_M\\d{2}");
	}
	
	public static boolean isBranchService(String node) {
		if(node.matches("\\d{2}"))
			return true;
		return false;
	}
	
	public static boolean isFailureDetectionServer(String server) {
		return server.startsWith("F") && server.charAt(3)=='_';
	}

	public static boolean isFailureDetectionService(String service) {
		return service.startsWith("F") && service.length()==3;
	}

	public static Type getType(String nodeName) {
		if(isGui(nodeName))
			return Type.GUI;
		if(isBranchServer(nodeName))
			return Type.BRANCHSERVER;
		if(isBranchService(nodeName))
			return Type.BRANCHSERVICE;
		if(isSensor(nodeName))
			return Type.SENSOR;
		if(isFailureDetectionServer(nodeName))
			return Type.FAILUREDETECTIONSERVER;
		if(isFailureDetectionService(nodeName))
			return Type.FAILUREDETECTIONSERVICE;
		
		return null;
	}
	
	private static boolean isSensor(String nodeName) {
		return nodeName.startsWith("R") && nodeName.charAt(3)=='_';
	}

	public static String getFailureDetectionService(String failureDetServer) {
		if(!failureDetServer.startsWith("F") || failureDetServer.charAt(3)!='_' || failureDetServer.length()!=7)
			return null;
		return failureDetServer.substring(0, 3);
	}
	
	public static String getGuiForService(String service) {
		return "G" + service.substring(0, 2);
	}
	
	public static String getGuiForServer(String server) {
		return "G" + server.substring(0, 2);
	}
	
	public static String getServiceForGui(String gui) {
		return gui.substring(1);
	}
	
	public static String getServiceForServer(String server) {
		return server.split("_")[0];
	}
	
	public static String getMachineForServer(String server) {
		return server.split("_")[1];
	}

	public static String getFailureDetectionServer(
			Vector<String> serversForMachine) {
		for (String server : serversForMachine) {
			if(isFailureDetectionServer(server))
				return server;
		}
		return null;
	}

	public static String getSensor(String failureDetectionServer) {
		return "R" + failureDetectionServer.substring(1);
	}

	public static String getFailureDetectionServer(String sensorName) {
		return "F" + sensorName.substring(1);
	}
	
}