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
 * 
 */
public class MachineProperties {
	private final Topology topology_;
	private final NodeLocations serverLocations_;	
	private ServiceConfig serviceConfig_;
	private String machine_;

	private String topologyFile_;		//  topology.txt
	private String serverLocationFile_;	//  servers.txt 
	private String serviceConfigFile_;	//  config.txt

	public static class PropertiesException extends Exception {
		public PropertiesException(String error) {
			super(error);
		}
	}

	public MachineProperties(String[] args) throws PropertiesException {
		topologyFile_ = "";
		serverLocationFile_ = "";

		try {
			parseCommandLine(args);
		} catch (FlagParser.FlagParseException fe) {
			throw new PropertiesException(fe.getMessage());
		}

		// Topology.
		try {
			if(topologyFile_.equals("")) {
				throw new PropertiesException("No topology file.");
			}			
			topology_ = new Topology(topologyFile_);
		} catch (IOException e) {
			throw new PropertiesException(e.getMessage());
		}


		// Server-Locations.
		try {
			if (serverLocationFile_.equals("")) {
				throw new PropertiesException("No server-location file.");
			}
			serverLocations_ = new NodeLocations(serverLocationFile_);
		} catch (IOException e) {
			throw new PropertiesException(e.getMessage());
		} 
		
		// Service-Configuration.
		try {
			if (serviceConfigFile_.equals("")) {
				throw new PropertiesException("No service-config file.");
			}
			serviceConfig_ = new ServiceConfig(serviceConfigFile_);
			serviceConfig_.parseConfigFile();
		} catch (IOException e) {
			throw new PropertiesException(e.getMessage());
		}
	}
	
	public MachineProperties(Topology tpl, NodeLocations locs,
			ServiceConfig sc, String machine) {
		topology_ = tpl;
		serverLocations_ = locs;
		serviceConfig_ = sc;
		machine_ = machine;
	}

	private void parseCommandLine(String[] args) throws	FlagParser.FlagParseException {
		FlagParser parser = new FlagParser();
		Vector<FlagParser.Argument> parsedArguments = parser.parseFlags(args);

		for (int i = 0; i < parsedArguments.size(); ++i) {
			FlagParser.Argument argument = parsedArguments.elementAt(i);

			try {
				if (argument.getName().equals("id")) {
					machine_ = argument.getValue();
				} else if (argument.getName().equals("topology")) {
					topologyFile_ = argument.getValue();
				} else if (argument.getName().equals("servers")) {
					serverLocationFile_ = argument.getValue();
				} else if (argument.getName().endsWith("config")) {
					serviceConfigFile_ = argument.getValue();
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
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig_;
	}
	
	public String getMachineName() {
		return machine_;
	}

	public String getTopologyFile() {
		return topologyFile_;
	}

	public String getServerLocationFile() {
		return serverLocationFile_;
	}
	
	public String getServiceConfigFile() {
		return serviceConfigFile_;
	}
}
