package branch.server;
/**
 * 
 * @author qsh2
 * 
 * Node structure of a branch server or a branch GUI.
 * 
 */
public class NodeName {
	private boolean gui_;
	private int branchId_;
	private int groupId_;
	
	public static String getService(String node) {
		if(node.startsWith("G"))
			return getServiceForGUI(node);
		else if(node.startsWith("S"))
			return getServiceForServer(node);
		return node;
	}
	
	public static String getGUI(String node) {
		if(node.startsWith("G"))
			return node;
		if(node.startsWith("S"))
			return getGUIForService(node);
		else 
			return getGUIForService(node);
	}
	
	public static boolean isGui(String node) {
		return node.startsWith("G");
	}
	
	public static boolean isServer(String node) {
		return node.startsWith("S");
	}
	
	public static boolean isService(String node) {
		if(!node.startsWith("G") && !node.startsWith("S"))
			return true;
		return false;
	}
	
	public static String getGUIForService(String service) {
		return "G"+service;
	}
	
	public static String getGUIForServer(String server) {
		return "G"+server.substring(1,3);
	}
	
	public static String getServiceForGUI(String gui) {
		return gui.substring(1);
	}
	
	public static String getServiceForServer(String server) {
		return server.substring(1,3);
	}
	
	public static String getServerForService(String service) {
		return "S" + service + "_01";
	}
	
	public static String getServerForGUI(String gui) {
		return "S" + gui.substring(1, 3) + "_01";
	}
	
	public NodeName() {
	}
	
	public NodeName(String str) {
		parseString(str);
	}

	public NodeName(boolean gui, int id) {
		gui_ = gui;
		branchId_ = id;
	}
	
	private boolean setBranchId(String str) {
		try {
			branchId_ = Integer.parseInt(str);	
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public boolean parseString(String str) {
		if (str.length() != 3) {
			return false;
		}
		
		if (str.startsWith("G")) {
			gui_ = true;
			if (setBranchId(str.substring(1))) {
				return true;
			}
		} else if (str.startsWith("S")) {
			gui_ = false;
			if (setBranchId(str.substring(1))) {
				return true;
			}
		}

		return false;
	}
		
	public String toString() {
		String str = "";
		if (gui_) {
			str += "G";
		} else {
			str += "S";
		}
		str += String.format("%02d", branchId_);
		
		return str;
	}
	
	public boolean isGui() {
		return gui_;
	}
	
	public int getBranchId() {
		return branchId_;
	}
	
	public boolean equals(NodeName node) {
		return (gui_ == node.gui_ && branchId_ == node.branchId_);
	}
	
}
