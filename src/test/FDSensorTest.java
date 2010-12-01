package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import junit.framework.TestCase;
import branch.server.FDSensor;

public class FDSensorTest extends TestCase {
	private static int default_timeout = 10000;
	private static int timeoutInc = 10;
	public static int pingtime = default_timeout - timeoutInc;
	private static String msgSeparator = "::";
	static Timer alivetimer = new Timer();
	HashMap<String, Timer> timers;
	FDSensor sensor;
	private Set<String> neighbors;
	private String myMachineName;
	

	public FDSensorTest() {
	}

	public FDSensorTest(String myMachineName, Set<String> neighbors) {
		this.myMachineName = myMachineName;
		this.neighbors = neighbors;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			neighbors = new HashSet<String>();
			neighbors.add("02");neighbors.add("03");neighbors.add("04");
			myMachineName = "01"; 

			sensor = new FDSensor(myMachineName, neighbors);
			Thread sthread = new Thread(sensor);
			sthread.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public final void testFDsens() {
/*		try {
			Socket destSocket = null;
			String str;
			
			Thread.sleep(4000);
			destSocket = new Socket("localhost", 10002);
			PrintWriter out = new PrintWriter(destSocket.getOutputStream(), true);
			str = "A"+ "::" + "ALIVE";
			out.println(str);
			out.close();
			destSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void testFDsensTopologyString() {
		try {
			Thread.sleep(default_timeout+5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Vector<String> expected = new Vector<String>();
		expected.add("B");
		expected.add("C");
		assertEquals(expected, sensor.getOutput()); */
	}

}
