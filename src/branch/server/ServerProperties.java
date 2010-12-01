package branch.server;


/**
 * 
 * @author qsh2
 *
 * This class maintains server specific properties.
 * It extends MachineProperties and shares a few
 * of the common properties.
 * 
 */
public class ServerProperties extends MachineProperties {
	private String ip_;
	private final int port_;
	private final boolean isGui_;

	private String serverName_;
	private String service_;
	private Integer sleep_ = 0; // sleep time in milliseconds

	public static enum ServerState {
		HEAD,
		MIDDLE,
		TAIL,
	}
	private ServerState serverState_;

	public ServerProperties(
			Topology tpl,
			NodeLocations locs,
			ServiceConfig sc,
			String machine,
			String serverName,
			boolean isGui) {
		super(tpl, locs, sc, machine);
				
		NodeLocations.Location loc = super.getServerLocations().getLocationForNode(serverName);
		port_ = loc.getPort();
		ip_ = loc.getIp();
		isGui_ = isGui;
		
		serverName_ = serverName;
		service_ = NodeName.getService(serverName);
		computeAndUpdateState();
		System.out.println(serverName_ + " is " + serverState_);
		// TODO: set sleep to 0 for now. Get it from the config instead.
		sleep_ = 0;
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
	
	public String getServerName() {
		return serverName_;
	}

	public String getServiceId() {
		return service_;
	}

	public Integer getSleep() {
		return sleep_;
	}

	public ServerState getState() {
		return serverState_;
	}
	
	public boolean isServiceReachable(String service) {
		View destView = getView(service);
		
		if (destView == null) return false;
		
		if (isGui_) {
			String guiServiceName = NodeName.getServiceForGui(serverName_);
			return guiServiceName.equals(service);
		} else {
			return super.getTopology().isServerReachable(serverName_, destView.getHead());
		}
	}
		
	public View getView(String service) {
		return super.getServiceConfig().getView(service);
	}

	public View getMyView() {
		// The updateView method should update the HashMap.
		// And successive calls to getView() should automatically
		// get the latest view for the current node.
		// Keeping a separate view_ will include extra state handling
		// (as the cache will go dirty).
		View myView = getServiceConfig().getView(service_);
		return myView;
	}

	public void updateView(View view) {
		ServerState oldState = getState();
		
		// Update the view.
		super.getServiceConfig().updateView(view);
		
		// Update the state of the server if relevant.
		computeAndUpdateState();
	}
	public void computeAndUpdateState() {
		ServerState oldState = getState();
		View view = getMyView();
			
		// Update the state of the server if relevant.
		if(service_.equalsIgnoreCase(view.getGroupId()) && !isGui_) {
			String myNewSuccessor = view.getSuccessor(serverName_);
			if(oldState == ServerState.TAIL) {
				if(myNewSuccessor != null) {
					if (view.getPredecessor(serverName_) != null) {
						updateState(ServerProperties.ServerState.MIDDLE);
					} else {
						updateState(ServerProperties.ServerState.HEAD);
					}
				}
			}
			else {
				if (myNewSuccessor == null) {
					updateState(ServerProperties.ServerState.TAIL);
				} else if (view.getPredecessor(serverName_) == null) {
					updateState(ServerProperties.ServerState.HEAD);
				} else {
					updateState(ServerProperties.ServerState.MIDDLE);
				}
			}
		}
	}
	public void updateState(ServerState state) {
		serverState_ = state;
	}

	public String print() {
		return serverName_.toString() + " starting at port:" + port_ + ". This server is part of service: " + service_;
	}

	/**
	 * @return time in milliseconds to sleep.
	 */
	public Integer getSleepTime() {
		return sleep_;
	}
}