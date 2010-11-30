package test;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import branch.server.NodeLocations;
import junit.framework.TestCase;


public class NodeLocationsTest extends TestCase {
	private File tempFile_;
	
	protected void setUp() throws Exception {
		tempFile_ = File.createTempFile("locations", ".txt");
		
		String str = "";
		str += "S01_M01 localhost 10001\n";
		str += "S02_M01 11.12.13.14 10002\n";
		str += "S03_M02 12.13.14.15 10004\n";
		str += "S04_M04 localhost 10005\n";
		try {
			FileWriter fw = new FileWriter(tempFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	protected void tearDown() {
		tempFile_.delete();
	}
	
	public void testLocation() {
		NodeLocations locations = null;
		try {
			locations = new NodeLocations(tempFile_.getAbsolutePath());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		String node = "S01_M01";
		NodeLocations.Location l = locations.getLocationForNode(node);
		assertEquals("localhost", l.getIp());
		assertEquals(10001, l.getPort());
		
		node = "S02_M01";
		l = locations.getLocationForNode(node);
		assertEquals("11.12.13.14", l.getIp());
		assertEquals(10002, l.getPort());
		
		node = "S03_M02";
		l = locations.getLocationForNode(node);
		assertEquals("12.13.14.15", l.getIp());
		assertEquals(10004, l.getPort());
		
		node = "S04_M04";
		l = locations.getLocationForNode(node);
		assertEquals("localhost", l.getIp());
		assertEquals(10005, l.getPort());
	}
	
	public void testServersForMachine() {
		NodeLocations locations = null;
		try {
			locations = new NodeLocations(tempFile_.getAbsolutePath());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Vector<String> servers;
		
		servers = locations.getServersForMachine("M01");
		assertEquals(2, servers.size());
		assertEquals("S01_M01", servers.get(0));
		assertEquals("S02_M01", servers.get(1));
		
		servers = locations.getServersForMachine("M02");
		assertEquals(1, servers.size());
		assertEquals("S03_M02", servers.get(0));
		
		servers = locations.getServersForMachine("M03");
		assertEquals(0, servers.size());
		
		servers = locations.getServersForMachine("M04");
		assertEquals(1, servers.size());
		assertEquals("S04_M04", servers.get(0));
	}
	
	
}
