package branch.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class FDServer implements Runnable {

	private static String serverInitMsg = "INIT_FROM_SERVER";
	private static String sensorInitMsg = "INIT_FROM_SENSOR";
	private static String msgSeparator = "::";
	

	private FDSensor sensor;
	private Vector<String> prev_suspects;
	private Topology topology;
	private boolean received_init = false;
	public static int sleep_interval = 10000;
	ArrayList<String> neighbors;
	Semaphore listenSema = new Semaphore(0);
	Semaphore initSema = new Semaphore(0);
	private boolean init_from_server = false;
	private boolean init_from_sensor = false;
	HashMap<String, ArrayList<String>> vp;
	private ServerSocket serverSocket;
	private NodeProperties properties_;

	public FDServer(NodeProperties properties, FDSensor sensor) {

		vp = new HashMap<String, ArrayList<String>>();
		this.topology = properties.getTopology();
		this.properties_ = properties;
		this.sensor = sensor;
		neighbors = topology.getInNeighbors();
		prev_suspects = new Vector<String>();
		try {
			serverSocket = new ServerSocket(10002);
		} catch (IOException e) {
			//TODO complete the error msg
			System.err.println("Cant create socket for ...");
			e.printStackTrace();
		}

	}

	public void run() {
		while(true) {
			try {
				//				Thread.sleep(sleep_interval);
				//				Vector<String> newsuspects = sensor.getOutput();
				//				if(newsuspects.equals(prev_suspects) && !received_init)
				//					continue;
				serverSocket.setSoTimeout(0);
				initSema.wait();

				if(init_from_sensor) {
					sendinit();
				}
				
				sendSuspects();
				listenSema.wait();
				consesusProtocol();
				//TODO IF the FDServer detects a addition/deletion
				// in a push model it needs to inform everyone

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void sendSuspects() {
		//TODO my name
		String msg = properties_.getNode();
		msg = msg + msgSeparator + "SUSPECTS";
		Vector<String> suspects = sensor.getOutput();
		for (String suspect : suspects) {
			msg += msgSeparator + suspect;
		}
		for (String neighbour : neighbors) {
			//TODO networkwrapper.send(msg, neighbors)
		}
	}
	
	private void consesusProtocol (){
		//TODO iterate over Vp
		// Check 
	}
	private void sendinit() {
		// send server init msg to all FD servers
		ArrayList<String> inNeighbors = topology.getInNeighbors();
		for (String neighbor : inNeighbors) {
			// TODO send the init message.
		}
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
					//TODO get the msg correct
					System.err.println("SOMe error");
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
					// TODO parse meg and set Vp
//					Vector<String> parseStr(str);
					receivedCount += 1;
					if (receivedCount == neighbors.size()- 1) {
						listenSema.notify();
					}	
				}
				
			}
		}
	}
}
