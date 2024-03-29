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
		ServerProperties sp;
		for (String server : servers) {
			Type type = NodeName.getType(server);
			switch(type) {
			case BRANCHSERVER:
				sp = new ServerProperties(
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
				
				ServerProperties spSensor = new ServerProperties(
						machineProp_.getTopology(),
						machineProp_.getServerLocations(),
						machineProp_.getServiceConfig(),
						machineProp_.getMachineName(),
						NodeName.getSensor(server),
						false);
				
				ServerProperties spFDServer = new ServerProperties(
						machineProp_.getTopology(),
						machineProp_.getServerLocations(),
						machineProp_.getServiceConfig(),
						machineProp_.getMachineName(),
						server,
						false);
				
				FDSensor sensor = new FDSensor(spSensor, spFDServer.getPort());
				Thread sthread = new Thread(sensor);
				sthread.start();

				FDServer fdserver = new FDServer(spFDServer, sensor);
				Thread fdThread = new Thread(fdserver);
				fdThread.start();
				break;

			case SENSOR:
				break;
				
			default:
				System.err.println("Error occurred. Invalid kind of server : " + server);
			}

		}
	}
}
