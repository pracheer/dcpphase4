package branch.server;

import java.util.Set;
import java.util.Vector;

import branch.server.NodeName.Type;

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

		for (String server : servers) {
			Type type = NodeName.getType(server);
			switch(type) {
			case BRANCHSERVER:
				ServerProperties sp = new ServerProperties(
						machineProp_.getTopology(),
						machineProp_.getServerLocations(),
						machineProp_.getServiceConfig(),
						machineProp_.getMachineName(),
						server,
						false);
				BranchServerThread branchServer = new BranchServerThread(sp);
				branchServer.start();
				break;

			case FAILUREDETECTIONSERVER:
				Set<String> neighbors = machineProp_.getTopology().getNeighbors(machineProp_.getMachineName());
				FDSensor sensor = new FDSensor(machineProp_.getMachineName(), neighbors);
				Thread sthread = new Thread(sensor);
				sthread.start();

				NodeLocations.Location loc = machineProp_.getServerLocations().getLocationForNode(server);
				FDServer fdserver = new FDServer(machineProp_, sensor, loc.getPort());
				Thread fdThread = new Thread(fdserver);
				fdThread.start();
				break;

			default:
				System.err.println("Error occurred. Invalid kind of server : " + server);
			}

		}
	}
}
