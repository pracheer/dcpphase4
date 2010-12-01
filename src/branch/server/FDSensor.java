package branch.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import branch.server.NodeLocations.Location;

public class FDSensor implements Runnable{

	// TODO
	public static int default_timeout = 10000;
	private static int timeoutInc = 10;
	public static int pingtime = default_timeout/2/* - timeoutInc*/;
	private static String msgSeparator = "::";

	Vector<String> output_;
	ArrayList<String> neighbors_;
	HashMap<String, Integer> timeouts_;
	HashMap<String, Date> lastListen;
	String mySensorname_;
	ServerProperties properties_;
	String myMachineName_;
	static Timer alivetimer = new Timer();
	HashMap<String, Timer> timers;

	public FDSensor(ServerProperties properties) {
		output_ = new Vector<String>();
		mySensorname_ = properties.getServerName();
		myMachineName_ = properties.getMachineName();
		View myView = properties.getMyView();
		neighbors_ = myView.getListOfServers();

		// remove the current sensor itself from the view.
		neighbors_.remove(properties.getServerName());

		properties_ = properties;

		timers = new HashMap<String, Timer>();
		output_ = new Vector<String>();
		lastListen = new HashMap<String, Date>();
		timeouts_ = new HashMap<String, Integer>();

		for (String neighbor : neighbors_) {
			String neighborMachine = NodeName.getMachineForServer(neighbor);
			timeouts_.put(neighborMachine, default_timeout);
			Timer timer = new Timer();
			timer.schedule(new TimeoutCheck(neighborMachine), default_timeout);
			timers.put(neighborMachine, timer);
		}
	}
	
	@Override
	public void run() {
		alivetimer.schedule(new AliveMsg(), pingtime);
		new ListeningThread().start();
	}


	class TimeoutCheck extends TimerTask {

		String machinename_;

		public TimeoutCheck(String machinename) {
			this.machinename_ = machinename;
		}

		public void run() {
			if(!output_.contains(machinename_)) {
				output_.add(machinename_);
				System.out.println("timeout for machine:" + machinename_);
			}
		}
	}

	public class ListeningThread extends Thread {
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(properties_.getPort());
				while (true) {
					Socket clientSocket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String str = in.readLine();
					if(str!=null) {
						String[] tokens = str.split(msgSeparator);
						String machinename = tokens[0];
						if (tokens.length != 2) {
							System.err.println("Invalid alive message:" + str);
						}
						clientSocket.close();

						lastListen.put(machinename, new Date());

						if(output_.contains(machinename)){
							output_.remove(machinename);
							timeouts_.put(machinename, timeouts_.get(machinename) + timeoutInc);
						}
						Timer timer = timers.get(machinename);
						timer.schedule(new TimeoutCheck(machinename), timeouts_.get(machinename));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<String> getOutput() {
		return output_;
	}

	public class AliveMsg extends TimerTask {
		public void run() {
			String msg = myMachineName_ + msgSeparator + "alive";
			try {
				for (String neighbor : neighbors_) {
					System.out.println(neighbor);
					Location loc = properties_.getServerLocations().getLocationForNode(neighbor);
					Socket socket;
					try {
						socket = new Socket(loc.getIp(), loc.getPort());
					} catch (Exception e) {
						System.err.println(neighbor + " is not reachable.");
						continue;
					}
					OutputStream oStream = socket.getOutputStream();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(oStream));
					writer.write(msg);
					writer.flush();
					writer.close();
				}

				if (mySensorname_.equals("R02_M04")) {
					Thread.sleep(90000);
				}
				
				alivetimer.schedule(new AliveMsg(), pingtime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*	public static void main(String[] args) {
		try {
//			Thread.sleep(10);

			HashSet<String> neighbors = new HashSet<String>() ;
			neighbors.add("02");neighbors.add("03");neighbors.add("04");
			String myMachineName = "01"; 
			FDSensor fdsens = new FDSensor(myMachineName, neighbors);
			Thread sthread = new Thread(fdsens);
			sthread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */

}
