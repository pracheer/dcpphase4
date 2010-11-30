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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class FDServer implements Runnable {

	private static String serverInitMsg = "INIT_FROM_SERVER";
	private static String sensorInitMsg = "INIT_FROM_SENSOR";

	private FDSensor sensor;
	private String myName;
	private Vector<String> prev_suspects;
	private Topology topology;
	private boolean received_init = false;
	public static int sleep_interval = 10000;
	ArrayList<String> neighbors;
	Semaphore listenSema = new Semaphore(0);
	Semaphore initSema = new Semaphore(0);
	private boolean init_from_server = false;
	private boolean init_from_sensor = false;
	HashMap<String, Vector<String>> vp;
	private ServerSocket serverSocket;
	private NodeProperties properties_;

	public FDServer(NodeProperties properties, FDSensor sensor, int port) {

		vp = new HashMap<String, Vector<String>>();
		this.topology = properties.getTopology();
		this.properties_ = properties;
		myName = properties_.getNode();
		this.sensor = sensor;
		neighbors = topology.getInNeighbors();
		prev_suspects = new Vector<String>();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			//TODO complete the error msg
			System.err.println("Cant create socket for ...");
			e.printStackTrace();
		}
	}

	public void run() {
		while(true) {
			try {
				serverSocket.setSoTimeout(0);
				initSema.wait();

				if(init_from_sensor) {
					// send server init msg to all FD servers
					sendToDummyNeighbors(serverInitMsg);
				}
				
				Vector<String> proposed_suspects = sensor.getOutput();
				vp.put(myName, proposed_suspects);

				// send this servers suspect list to all neighbors.
				Suspects obj = new Suspects(myName, proposed_suspects);
				String msg = obj.toString();
				sendToDummyNeighbors(msg);

				// TODO check the socket timeout.
				serverSocket.setSoTimeout(sleep_interval);
				listenSema.wait();
				Vector<String> new_suspects = consensusProtocol();
				if(!new_suspects.equals(prev_suspects)) {
					// update views
					//TODO need to send the new suspects to all the branch servers running on this machine.
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendToDummyNeighbors(String msg) {
		try {
/*			ArrayList<String> inNeighbors = topology.getInNeighbors();
			for (String neighbor : inNeighbors) {
				// TODO send the init message.
			}
*/
			Socket socket = new Socket("localhost", 10003);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);
			
			socket = new Socket("localhost", 10004);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);
			
			socket = new Socket("localhost", 10005);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);
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
					clientSocket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
					str = in.readLine();
				} catch (SocketTimeoutException  e) {
					listenSema.notify();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(str.equals(serverInitMsg) && !init_from_server && !init_from_sensor) {
					init_from_server = true;
					receivedCount = 0;
					initSema.notify();
				}
				else if (str.equals(sensorInitMsg) && !init_from_sensor && !init_from_server) {
					init_from_sensor = true;
					receivedCount = 0;
					initSema.notify();
				} else {
					if(str.equals(serverInitMsg) || str.equals(sensorInitMsg))
						continue;
					
					Suspects objsuspects = Suspects.parseSuspectsString(str);
					
					vp.put(objsuspects.getNodeName(), objsuspects.getSuspects());
					
					receivedCount += 1;
					if (receivedCount == neighbors.size()- 1) {
						listenSema.notify();
					}	
				}
			}
		}
	}
}
