package branch.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Maintains the network topology.
 * 
 *  isMachineReachable returns the connectivity between to machines / processors.
 *  If two servers are running in two connected machines, they are connected.
 * 
 * @author qsh2
 * 
 */

public class Topology {
	public static class Connection {
		String srcService_;
		String destService_;

		public Connection() {
			srcService_ = null;
			destService_ = null;
		}

		// Parses string of the form, JVM1 JVM2
		// and updates the connection variables.
		public void parseString(String str) {
			int spaceAt = str.indexOf(' ');

			srcService_ = str.substring(0, spaceAt);
			destService_ = str.substring(spaceAt + 1);
		}

		public String getSourceString() {
			return srcService_.toString();
		}

		public String getDestinationString() {
			return destService_.toString();
		}
	}

	final private ArrayList<Connection> connections_;

	public Topology(String topologyFileLocation) throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(new File(topologyFileLocation));
		} catch (FileNotFoundException e) {
			throw new IOException(e.getMessage());
		}

		connections_ = new ArrayList<Connection>();

		BufferedReader in = new BufferedReader(fr);
		String str;
		while ((str = in.readLine())!=null) {
			if(str.startsWith(Constants.COMMENT_START) || str.isEmpty())
				continue;

			Connection c = new Connection();
			c.parseString(str);
			connections_.add(c);
		}

		in.close();
		fr.close();
	}

	public boolean isMachineReachable(String fromMachine, String toMachine) {
		if(fromMachine.equalsIgnoreCase(toMachine))
			return true;

		for (int i = 0; i < connections_.size(); ++i) {
			final Connection con = connections_.get(i);

			if (con.getSourceString().equalsIgnoreCase(fromMachine) &&
					con.getDestinationString().equalsIgnoreCase(toMachine)) {
				return true;
			}
			// To insure bidirectional connection
			if (con.getSourceString().equalsIgnoreCase(toMachine) &&
					con.getDestinationString().equalsIgnoreCase(fromMachine)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isServerReachable(String fromServer, String toServer) {
		if (NodeName.isGui(fromServer) || NodeName.isGui(toServer)) {
			String fromService = NodeName.getService(fromServer);
			String toService = NodeName.getService(toServer);
			return fromService.equals(toService);
		} else {
			String fromMachine = NodeName.getMachineForServer(fromServer);
			String toMachine = NodeName.getMachineForServer(toServer);

			return isMachineReachable(fromMachine, toMachine);
		}
	}
}
