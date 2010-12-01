package branch.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class ServiceConfig {
	private String serviceConfigFile_;
	private HashMap<String, View> views_ = new HashMap<String, View>(10);

	public ServiceConfig(String configFile) {
		serviceConfigFile_ = configFile;
	}

	public ServiceConfig clone() {
		ServiceConfig serviceConfig = new ServiceConfig(this.serviceConfigFile_);
		serviceConfig.views_ = new HashMap<String, View>();
		Set<String> keys = views_.keySet();
		for (String key : keys) {
			View view = views_.get(key);
			View viewNew = new View(view.groupId_);
			ArrayList<String> oldServers = view.getListOfServers();
			ArrayList<String> newServers = new ArrayList<String>();
			for (String server : oldServers) {
				newServers.add(server);
			}
			viewNew.listOfServers = newServers;
			serviceConfig.views_.put(key, viewNew);
		}
		
		return serviceConfig;
	}

	public void parseConfigFile() throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(new File(serviceConfigFile_));
		} catch (FileNotFoundException e) {
			throw new IOException(e.getMessage());
		}

		BufferedReader in = new BufferedReader(fr);
		String str;
		while ((str = in.readLine()) != null) {
			if(str.startsWith(Constants.COMMENT_START) || str.isEmpty())
				continue;

			int colonPos = str.indexOf(':');

			if (colonPos < 0) {
				System.err.println("Could not parse config line: " + str);
				continue;
			}

			String service = str.substring(0, colonPos);
			View currView = new View(service);

			String[] servers = str.substring(colonPos+1).split(" ");

			for (int i = 0; i < servers.length; ++i) {
				if (!servers[i].isEmpty()) {
					currView.addServer(servers[i]);
				}
			}

			views_.put(service, currView);
		}
	}

	public View getView(String service) {
		return views_.get(service);
	}

	public void updateView(View newView) {
		views_.put(newView.getGroupId(), newView);
	}

	public HashMap<String, View> getViews() {
		return views_;
	}
}
