package branch.server;
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
	public static String getService(String node) {
		if(NodeName.isGui(node))
			return getServiceForGui(node);
		else if(NodeName.isServer(node))
			return getServiceForServer(node);
		return node;
	}
	
	public static String getGui(String node) {
		if(NodeName.isGui(node))
			return node;
		else if(NodeName.isServer(node))
			return getGuiForService(node);
		
		System.err.println("getGUI called for non-server / non-gui node.");
		return node;
	}
	
	public static boolean isGui(String node) {
		return node.startsWith("G");
	}
	
	public static boolean isServer(String node) {
		return node.startsWith("S") && node.matches("S\\d{2}_M\\d{2}");
	}
	
	public static boolean isService(String node) {
		if(node.startsWith("S") && node.matches("S\\d{2}"))
			return true;
		return false;
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
}