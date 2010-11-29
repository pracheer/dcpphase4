package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.Vector;

import branch.server.FDSensor;
import branch.server.FDSensor.AliveMsg;
import branch.server.FDSensor.ListeningThread;
import junit.framework.TestCase;

public class FDsensTest extends TestCase {
	private static int default_timeout = 10000;
	private static int timeoutInc = 10;
	public static int pingtime = default_timeout - timeoutInc;
	private static String msgSeparator = "::";
	static Timer alivetimer = new Timer();
	HashMap<String, Timer> timers;
	FDSensor sensor = new FDSensor();
	

	public FDsensTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			//alivetimer.schedule(sensor.new AliveMsg(), pingtime);
			sensor.new ListeningThread().start();
			System.out.println("main over");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public final void testFDsens() {
		try {
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
		assertEquals(expected, sensor.getOutput());
	}

}
