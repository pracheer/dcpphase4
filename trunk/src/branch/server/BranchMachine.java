package branch.server;

import java.util.Vector;

public class BranchMachine {
	public static MachineProperties machineProp_;

	public static void main(String[] args) {
		// Parse the flags to get the arguments.
		machineProp_ = null;
		try {
			machineProp_ = new MachineProperties(args);
		} catch (MachineProperties.PropertiesException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		NodeLocations locs = machineProp_.getServerLocations();
		Vector<String> servers = locs.getServersForMachine(machineProp_.getMachineName());
		
		for (int i = 0; i < servers.size(); ++i) {
			ServerProperties sp = new ServerProperties(
					machineProp_.getTopology(),
					machineProp_.getServerLocations(),
					machineProp_.getServiceConfig(),
					machineProp_.getMachineName(),
					servers.get(i),
					false);
			BranchServerThread server = new BranchServerThread(sp);
			
			server.start();
		}
	}
}
