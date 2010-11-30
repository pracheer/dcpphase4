package test;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import branch.server.FlagParser;
import branch.server.NetworkWrapper;
import branch.server.NodeName;
import branch.server.ServerProperties;
import branch.server.Trxn;

public class Test {

	public static final String COMMENT_START = "#";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
/*
		try {
			TestProperties properties = new TestProperties(args);
			BufferedReader reader = new BufferedReader(new FileReader(properties.testFile_));

			String str;
			while((str = reader.readLine())!= null) {
				if(str.startsWith(COMMENT_START) || str.isEmpty())
					continue;
				str = str.trim();
				int index1 = str.indexOf(Trxn.msgSeparator);
				int sleepTime = Integer.parseInt(str.substring(0, index1));
				int index2 = str.indexOf(Trxn.msgSeparator, index1 + Trxn.msgSeparator.length());
				String destNode = str.substring(index1 + Trxn.msgSeparator.length(), index2);
				String msgStr = str.substring(index2 + Trxn.msgSeparator.length());
				Thread.sleep(sleepTime);
				args = ("-id "+destNode+" -topology "+properties.topologyFile_+" -servers "+properties.serverLocationFile_).split(" ");
				ServerProperties properties_ = new ServerProperties(args, true);
				NetworkWrapper.setProperties(properties_);
				NetworkWrapper.send(msgStr, NodeName.getServerForService(destNode));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		*/
	}
}

/**
 * 
 * @author qsh2
 *
 * This class uses the FlagParser to parse the flags.
 * From the FlagParser class we receive a vector of program arguments which is
 * (argument-name, argument-value) pairs.
 * From that vector, this class populates the related properties fields.
 * 
 */
class TestProperties {
	String topologyFile_;
	String serverLocationFile_;
	String testFile_;

	public static class NodePropertiesException extends Exception {
		public NodePropertiesException(String error) {
			super(error);
		}
	}

	public TestProperties(String[] args) throws NodePropertiesException {
		topologyFile_ = "";
		serverLocationFile_ = "";
		testFile_ = "";

		try {
			parseCommandLine(args);
		} catch (FlagParser.FlagParseException fe) {
			throw new NodePropertiesException(fe.getMessage());
		}

		if(topologyFile_.equals(""))
			throw new NodePropertiesException("No topology file.");

		if(testFile_.equals(""))
			throw new NodePropertiesException("No test file.");


		if (serverLocationFile_.equals("")) {
			throw new NodePropertiesException("No server-location file.");
		}
	}

	private void parseCommandLine(String[] args) throws	FlagParser.FlagParseException {
		FlagParser parser = new FlagParser();
		Vector<FlagParser.Argument> parsedArguments = parser.parseFlags(args);

		for (int i = 0; i < parsedArguments.size(); ++i) {
			FlagParser.Argument argument = parsedArguments.elementAt(i);

			try {
				if (argument.getName().equals("topology")) {
					topologyFile_ = argument.getValue();
				} else if (argument.getName().equals("servers")) {
					serverLocationFile_ = argument.getValue();
				} else if (argument.getName().endsWith("testfile")) {
					testFile_ = argument.getValue();
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

}