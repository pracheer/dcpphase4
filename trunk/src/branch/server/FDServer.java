package branch.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import branch.server.NodeLocations.Location;

public class FDServer implements Runnable {

	private static int sleep_interval = 10000;
	private static String serverInitMsg = "INIT_FROM_SERVER";
	private static String sensorInitMsg = "INIT_FROM_SENSOR";

	private FDSensor sensor_;
	private Vector<String> prev_suspects_;
	private ArrayList<String> neighbors_;
	private Semaphore listenSema_ = new Semaphore(0);
	private Semaphore initSema_ = new Semaphore(0);
	private Semaphore mutex = new Semaphore(0);
	private boolean initFromServer_ = false;
	private boolean initFromSensor_ = false;
	private HashMap<String, Vector<String>> vp;
	private ServerSocket serverSocket_;
	private ServerProperties properties_;
	private NetworkWrapper netWrapper_;

	public FDServer(ServerProperties properties, FDSensor sensor) {
		vp = new HashMap<String, Vector<String>>();
		properties_ = properties;

		netWrapper_ = new NetworkWrapper(properties);
		
		// Get the neighborlist. Remove myself from the list.
		neighbors_ = properties.getMyView().getListOfServers();
		neighbors_.remove(properties_.getServerName());

		sensor_ = sensor;
		prev_suspects_ = new Vector<String>();
		
		try {
			serverSocket_ = new ServerSocket(properties.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		Vector<String> machinesToAdd = new Vector<String>();
		Vector<String> machinesToRemove = new Vector<String>();
		
		
		while(true) {
			try {
				serverSocket_.setSoTimeout(0);
				synchronized (initSema_) {
					initSema_.wait();
				}

				if(initFromSensor_) {
					// send server init msg to all FD servers
					sendToNeighbors(serverInitMsg);
				}

				Vector<String> proposed_suspects = sensor_.getOutput();
				vp.put(properties_.getMachineName(), proposed_suspects);

				// send this servers suspect list to all neighbors.
				Suspects obj = new Suspects(properties_.getMachineName(), proposed_suspects);
				String msg = obj.toString();
				sendToNeighbors(msg);

				// TODO check the socket timeout.
				serverSocket_.setSoTimeout(sleep_interval);
				synchronized (initSema_) {
					listenSema_.wait();
				}
				Vector<String> new_suspects = consensusProtocol();
				Collections.sort(new_suspects);
				Collections.sort(prev_suspects_);
				
				machinesToAdd.clear();
				machinesToRemove.clear();
				
				getMachinesToChange(prev_suspects_, new_suspects, machinesToAdd, machinesToRemove);
				
				Vector<View> viewsToUpdate = getViewsToUpdate(machinesToAdd, machinesToRemove);
				Vector<String> serversInMyMachine =
					properties_.getServerLocations().getServersForMachine(properties_.getMachineName());
				for (View v : viewsToUpdate) {
					for (String interestedServer : serversInMyMachine) {
						if (NodeName.getType(interestedServer) != NodeName.Type.BRANCHSERVER) {
							continue;
						}
						
						SpecialMsg sm = new SpecialMsg(v);
						Message uvMsg = new Message(properties_.getServerName(), sm);
						
						netWrapper_.sendToServer(uvMsg.toString(), interestedServer);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			initFromSensor_ = false;
			initFromServer_ = false;
		}
	}
	
	public static void getMachinesToChange(
			Vector<String> prvSuspects,
			Vector<String> newSuspects,
			Vector<String> machinesToAdd,
			Vector<String> machinesToRemove) {
		machinesToAdd.clear();
		machinesToRemove.clear();
		
		
		for (String newS : newSuspects) {
			if (!prvSuspects.contains(newS)) {
				machinesToAdd.add(newS);
			}
		}
		
		for (String prvS : prvSuspects) {
			if (!newSuspects.contains(prvS)) {
				machinesToRemove.add(prvS);
			}
		}
	}

	
	public Vector<View> getViewsToUpdate(
			Vector<String> toAdd,
			Vector<String> toRem) {
		HashSet<String> updatedViews = new HashSet<String>();
		Vector<View> viewsToUpdate = new Vector<View>();
		
		for (String addedMachine : toAdd) {
			Vector<String> serversInMachine =
				properties_.getServerLocations().getServersForMachine(addedMachine);

			for (String affectedServer : serversInMachine) {
				if (NodeName.getType(affectedServer) != NodeName.Type.BRANCHSERVER) {
					continue;
				}

				String affectedService = NodeName.getService(affectedServer);			
				View v = properties_.getServiceConfig().getView(affectedService);
				v.addServer(affectedServer);
				
				if (!updatedViews.contains(v.getGroupId())) {
					viewsToUpdate.add(v);
				}
			}
		}
		
		
		for (String removedMachine : toRem) {
			Vector<String> serversInMachine =
				properties_.getServerLocations().getServersForMachine(removedMachine);

			for (String affectedServer : serversInMachine) {
				if (NodeName.getType(affectedServer) != NodeName.Type.BRANCHSERVER) {
					continue;
				}

				String affectedService = NodeName.getService(affectedServer);			
				View v = properties_.getServiceConfig().getView(affectedService);
				v.removeServer(affectedServer);
				
				if (!updatedViews.contains(v.getGroupId())) {
					viewsToUpdate.add(v);
				}
			}
		}
		
		return viewsToUpdate;
	}

	private void sendToNeighbors(String msg) {
		try {
			for (String neighbor : neighbors_) {
				Location locationForNode = properties_.getServerLocations().getLocationForNode(neighbor);
				Socket socket = new Socket(locationForNode.getIp(), locationForNode.getPort());
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.println(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Vector<String> consensusProtocol (){
		Set<String> machines = vp.keySet();
		Set<String> setOfSuspects = new HashSet<String>();
		Collection<Vector<String>> vectors = vp.values();

		for (String machine : machines) {
			Vector<String> machine_suspects = vp.get(machine);
			setOfSuspects.addAll(machine_suspects);
		}

		System.out.println("doing consensus on the suspected set");

		Vector<String> new_suspects = new Vector<String>();
		for (String suspect : setOfSuspects) {
			int count = 0;
			for (Vector<String> vector : vectors) {
				if(vector.contains(suspect))
					count +=1;
			}
			if(count >= (machines.size()/2 + 1)) { // majority protocol
				new_suspects.add(suspect);
			}
		}

		return new_suspects;
	}

	class listenInit implements Runnable {

		public void run() {
			int receivedCount = 0;
			String str = "";
			while(true) {
				Socket clientSocket;
				try {
					clientSocket = serverSocket_.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
					str = in.readLine();
				} catch (SocketTimeoutException  e) {
					synchronized (initSema_) {
						listenSema_.notify();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(str.equals(serverInitMsg) && !initFromServer_ && !initFromSensor_) {
					initFromServer_ = true;
					receivedCount = 0;
					synchronized (initSema_) {
						initSema_.notify();
					}
				}
				else if (str.equals(sensorInitMsg) && !initFromSensor_ && !initFromServer_) {
					initFromSensor_ = true;
					receivedCount = 0;
					synchronized (initSema_) {
						initSema_.notify();
					}
				} else {
					if(str.equals(serverInitMsg) || str.equals(sensorInitMsg))
						continue;

					Suspects objsuspects = Suspects.parseSuspectsString(str);

					vp.put(objsuspects.getNodeName(), objsuspects.getSuspects());

					receivedCount += 1;
					if (receivedCount == neighbors_.size()- 1) {
						synchronized (initSema_) {
							listenSema_.notify();
						}
					}	
				}
			}
		}
	}
}
