package branch.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Maintains the network topology.
 * Gives the isReachable() interface to the current node, so that it knows
 * which servers / GUIs are reachable by it. 
 * Also gives an easy interface for the snapshot generator to know the
 * incoming-channels and outgoing-channels for the current node.
 * This is given by the two methods (inNeighbors) and (outNeighbors). 
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
	final private ArrayList<String> inNeighbors_;
	final private ArrayList<String> outNeighbors_;
	final private String group_;

	public Topology(String topologyFileLocation, String group) throws IOException {
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

		group_ = group;
		inNeighbors_ = whoInNeighbors(group);
		outNeighbors_ = whoOutNeighbors(group);
	}

	public boolean isReachable(String nodeName) {
		String destService = NodeName.getService(nodeName);
		if(destService.equalsIgnoreCase(group_))
			return true;

		for (int i = 0; i < connections_.size(); ++i) {
			final Connection con = connections_.get(i);

			if (con.getSourceString().equalsIgnoreCase(group_) &&
					con.getDestinationString().equalsIgnoreCase(destService)) {
				return true;
			}
			// To insure bidirectional connection
			if (con.getSourceString().equalsIgnoreCase(destService) &&
					con.getDestinationString().equalsIgnoreCase(group_)) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<String> whoOutNeighbors(String src) {
		ArrayList<String> outNeighbors = new ArrayList<String>();

		for (int i = 0; i < connections_.size(); ++i) {
			final Connection curr = connections_.get(i);
			if (curr.getSourceString().equals(src) && curr.getDestinationString().charAt(0) != 'G') {
				outNeighbors.add(curr.getDestinationString());
			}
		}
		return outNeighbors;
	}

	private ArrayList<String> whoInNeighbors(String src) {
		ArrayList<String> inNeighbors = new ArrayList<String>();

		for (int i = 0; i < connections_.size(); ++i) {
			final Connection curr = connections_.get(i);
			if (curr.getDestinationString().equals(src) && curr.getSourceString().charAt(0) != 'G') {
				inNeighbors.add(curr.getSourceString());
			}
		}

		return inNeighbors;
	}

	public ArrayList<String> getInNeighbors() {
		return inNeighbors_;
	}

	public ArrayList<String> getOutNeighbors() {
		return outNeighbors_;
	}	
}
