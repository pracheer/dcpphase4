package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import junit.framework.TestCase;
import branch.server.FDSensor;
import branch.server.MachineProperties;
import branch.server.ServerProperties;

public class FDSensorTest extends TestCase {
	private static int default_timeout = 10000;
	private static int timeoutInc = 10;
	public static int pingtime = default_timeout - timeoutInc;
	private static String msgSeparator = "::";
	static Timer alivetimer = new Timer();
	HashMap<String, Timer> timers;
	FDSensor sensor;
	private ServerProperties properties_;
	private MachineProperties machineProp_;
	private File tempServerLocationFile_;
	private File tempTopologyFile_;
	private File tempServiceConfigFile_;
	private int fdServerPort;

	public FDSensorTest() {
	}

/*	public FDSensorTest(String myMachineName, Set<String> neighbors) {
		this.myMachineName = myMachineName;
		this.neighbors = neighbors;
	}
*/	
	protected void setUp() throws Exception {
		createProperties();		

		startSensor("R01_M01");
		startSensor("R01_M02");
		startSensor("R01_M03");
		startSensor("R01_M04");
	}

	private void startSensor(String sensorName) {
		String[] args = new String[8];
		args[0] = "-id";
		args[1] = sensorName;
		args[2] = "-topology";
		args[3] = tempTopologyFile_.getAbsolutePath();
		args[4] = "-servers";
		args[5] = tempServerLocationFile_.getAbsolutePath();
		args[6] = "-config";
		args[7] = tempServiceConfigFile_.getAbsolutePath();
		
		try {
			machineProp_ = new MachineProperties(args);
		} catch (MachineProperties.PropertiesException e) {
			System.err.println(e.getMessage());
			fail("Flag parser exception for valid command line.");
		}
		
		properties_ = new ServerProperties(
				machineProp_.getTopology(),
				machineProp_.getServerLocations(),
				machineProp_.getServiceConfig(),
				machineProp_.getMachineName(),
				sensorName,
				false);
		
		try {

			sensor = new FDSensor(properties_, fdServerPort);
			Thread sthread = new Thread(sensor);
			sthread.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createProperties() throws IOException {
		tempServerLocationFile_ = File.createTempFile("locations", ".txt");
		String str;
		
		str = "";
		str += "R01_M01 localhost 10001\n";
		str += "F01_M01 localhost 10002\n";
		str += "R01_M02 localhost 10003\n";
		str += "F01_M02 localhost 10004\n";
		str += "R01_M03 localhost 10005\n";
		str += "F01_M03 localhost 10006\n";
		str += "R01_M04 localhost 10007\n";
		str += "F01_M04 localhost 10008\n";
		try {
			FileWriter fw = new FileWriter(tempServerLocationFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		tempTopologyFile_ = File.createTempFile("topology", ".txt");
		
		str = "";
		str += "M01 M02\n";
		str += "M01 M03\n";
		str += "M01 M04\n";
		str += "M02 M03\n";
		str += "M02 M04\n";
		str += "M03 M04\n";

		try {
			FileWriter fw = new FileWriter(tempTopologyFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		tempServiceConfigFile_ = File.createTempFile("config", ".txt");
		
		str = "";
		str += "F01:F01_M01 F01_M02 F01_M03 F01_M04\n";
		try {
			FileWriter fw = new FileWriter(tempServiceConfigFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		properties_ = null;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
