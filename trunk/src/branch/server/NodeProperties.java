package branch.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author qsh2
 *
 * This class uses the FlagParser to parse the flags.
 * From the FlagParser class we receive a vector of program arguments which is
 * (argument-name, argument-value) pairs.
 * From that vector, this class populates the related properties fields.
 * The server maintains an object of this class so that everyone has access to common
 * elements like Topology or NodeLocations.
 * 
 */
public class NodeProperties {
	private final Topology topology_;
	private final NodeLocations serverLocations_;
	private String node_;
	private String ip_;
	private final int port_;
	private final boolean isGui_;
	public  String configFile_;

	public HashMap<String, View> views_ = 
		new HashMap<String, View>(10);

	private String groupId_;
	public void setGroupId_(String groupId_) {
		this.groupId_ = groupId_;
	}

	private String topologyFile_;
	private String serverLocationFile_;
	private Integer sleep_ = 0; // sleep time in milliseconds

	public static enum ServerState {
		HEAD,
		MIDDLE,
		TAIL,
	}

	private ServerState serverState_;  

	public static class NodePropertiesException extends Exception {
		public NodePropertiesException(String error) {
			super(error);
		}
	}

	public NodeProperties(String[] args, boolean isGui) throws NodePropertiesException {
		groupId_ = null;
		topologyFile_ = "";
		serverLocationFile_ = "";
		isGui_ = isGui;

		try {
			parseCommandLine(args);
		} catch (FlagParser.FlagParseException fe) {
			throw new NodePropertiesException(fe.getMessage());
		}

		// Topology.
		try {
			if(topologyFile_.equals("")) {
				throw new NodePropertiesException("No topology file.");
			}			
			topology_ = new Topology(topologyFile_, groupId_);
		} catch (IOException e) {
			throw new NodePropertiesException(e.getMessage());
		}


		// Server-Locations.
		try {
			if (serverLocationFile_.equals("")) {
				throw new NodePropertiesException("No server-location file.");
			}
			serverLocations_ = new NodeLocations(serverLocationFile_);
		} catch (IOException e) {
			throw new NodePropertiesException(e.getMessage());
		}

		port_ = serverLocations_.getLocationForNode(node_).getPort();
		ip_ = serverLocations_.getLocationForNode(node_).getIp(); 
	}

	private void parseCommandLine(String[] args) throws	FlagParser.FlagParseException {
		FlagParser parser = new FlagParser();
		Vector<FlagParser.Argument> parsedArguments = parser.parseFlags(args);

		for (int i = 0; i < parsedArguments.size(); ++i) {
			FlagParser.Argument argument = parsedArguments.elementAt(i);

			try {
				if (argument.getName().equals("id")) {
					node_ = argument.getValue();
				} else if (argument.getName().equals("topology")) {
					topologyFile_ = argument.getValue();
				} else if (argument.getName().equals("servers")) {
					serverLocationFile_ = argument.getValue();
				} else if (argument.getName().endsWith("sleep")) {
					sleep_ = Integer.parseInt(argument.getValue());
				} else if (argument.getName().endsWith("config")) {
					configFile_ = argument.getValue();
				} else if (argument.getName().endsWith("group")) {
					String expression = "^[0-9][0-9]$";
					Pattern pattern = Pattern.compile(expression);
					Matcher matcher = pattern.matcher(argument.getValue());
					if (!matcher.matches()) {
						throw new FlagParser.FlagParseException(
								"Incorrect GroupName format: " + argument.getValue());
					}					
					groupId_ = argument.getValue();
				} else {
					throw new FlagParser.FlagParseException(
							"Unknown flag: " + argument.getName());
				}
			} catch(NumberFormatException ne) {
				throw new FlagParser.FlagParseException(
						"Could not parse integer. " + ne.getMessage());
			} 
		}
	}

	public Topology getTopology() {
		return topology_;
	}

	public NodeLocations getServerLocations() {
		return serverLocations_;
	}

	public String getNode() {
		return node_;
	}

	public String getIp() {
		return ip_;
	}

	public int getPort() {
		return port_;
	}

	public boolean isGui() {
		return isGui_;
	}

	public String getGroupId() {
		return groupId_;
	}

	public String getTopologyFile() {
		return topologyFile_;
	}

	public String getServerLocationFile() {
		return serverLocationFile_;
	}

	public Integer getSleep() {
		return sleep_;
	}

	public ServerState getState() {
		return serverState_;
	}

	public View getMyView() {
		// DO NOT keep a local view_
		// The updateView method should update the HashMap.
		// And successive calls to getView() should automatically
		// get the latest view for the current node.
		// Keeping a separate view_ will include extra state handling
		// (as the cache will go dirty).
		View myView = views_.get(groupId_);

		return myView;
	}

	public void updateView(View view) {
		ServerState oldState = getState();
		views_.put(view.getGroupId(), view);
		if(groupId_.equalsIgnoreCase(view.getGroupId()) && !isGui_) {
			String myNewSuccessor = view.getSuccessor(node_);
			if(oldState == ServerState.TAIL) {
				if(myNewSuccessor != null) {
					if (view.getPredecessor(node_) != null) {
						updateState(NodeProperties.ServerState.MIDDLE);
					} else {
						updateState(NodeProperties.ServerState.HEAD);
					}
				}
			}
			else {
				if (myNewSuccessor == null) {
					updateState(NodeProperties.ServerState.TAIL);
				} else if (view.getPredecessor(node_) == null) {
					updateState(NodeProperties.ServerState.HEAD);
				} else {
					updateState(NodeProperties.ServerState.MIDDLE);
				}
			}
		}
	}

	public void updateState(ServerState state) {
		serverState_ = state;
	}

	public String print() {
		return node_.toString() + " starting at port:" + port_
		+ " using the topology file:" + topologyFile_
		+ ", servers file: " + serverLocationFile_
		+ " and sleep time of " + sleep_ + " milliseconds."
		+ " This server is part of group " + groupId_;
	}

	/**
	 * @return time in milliseconds to sleep.
	 */
	public Integer getSleepTime() {
		return sleep_;
	}
}