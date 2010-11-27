package test;
/**
 * 
 * @author qsh2
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import branch.server.NodeName;
import branch.server.NodeLocations;
import branch.server.NodeProperties;
import branch.server.Topology;
import junit.framework.TestCase;

public class NodePropertiesTest extends TestCase {
	private NodeProperties properties_;
	private File tempServerLocationFile_;
	private File tempTopologyFile_;
	
	public void setUp() throws Exception {
		tempServerLocationFile_ = File.createTempFile("locations", ".txt");
		String str;
		
		str = "";
		str += "S01 localhost 10001\n";
		str += "S02 11.12.13.14 10002\n";
		str += "S03 12.13.14.15 10004\n";
		str += "S04 localhost 10005\n";
		str += "G01 localhost 10006\n";
		try {
			FileWriter fw = new FileWriter(tempServerLocationFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		tempTopologyFile_ = File.createTempFile("topology", ".txt");
		
		str = "";
		str += "01 01\n";
		str += "01 01\n";
		str += "01 05\n";
		str += "02 04\n";
		str += "02 01\n";
		str += "02 03\n";
		str += "02 02\n";
		str += "02 02\n";
		
		try {
			FileWriter fw = new FileWriter(tempTopologyFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		properties_ = null;
	} 
	
	public void testValidNodeProperties() {
		String[] args = new String[8];
		
		args[0] = "-id";
		args[1] = "S02";
		args[2] = "-topology";
		args[3] = tempTopologyFile_.getAbsolutePath();
		args[4] = "-servers";
		args[5] = tempServerLocationFile_.getAbsolutePath();
		args[6] = "-group";
		args[7] = "02";
		
		try {
			properties_ = new NodeProperties(args, false);
		} catch (NodeProperties.NodePropertiesException e) {
			System.err.println(e.getMessage());
			fail("Flag parser exception for valid command line.");
		}
		
		assertEquals(10002, properties_.getPort());
		assertEquals("11.12.13.14", properties_.getIp());
		assertEquals("02", properties_.getGroupId());
		assertTrue(properties_.getNode().equals("S02"));
		
		Topology tpl = properties_.getTopology();
		assertTrue(tpl.isReachable("G02"));
//		assertTrue(tpl.isReachable("S01"));
//		assertTrue(tpl.isReachable("S03"));
//		assertFalse(tpl.isReachable("S05"));

		NodeLocations nl = properties_.getServerLocations();
		NodeLocations.Location l = nl.getLocationForNode("S01");
		assertEquals("localhost", l.getIp());
		assertEquals(10001, l.getPort());
		
		l = nl.getLocationForNode("G01");
		assertEquals("localhost", l.getIp());
		assertEquals(10006, l.getPort());
	}
	
	public void testValidGuiNodeProperties() {
		String[] args = new String[8];
		
		args[0] = "-id";
		args[1] = "G01";
		args[2] = "-topology";
		args[3] = tempTopologyFile_.getAbsolutePath();
		args[4] = "-servers";
		args[5] = tempServerLocationFile_.getAbsolutePath();
		args[6] = "-group";
		args[7] = "01";
		
		try {
			properties_ = new NodeProperties(args, true);
		} catch (NodeProperties.NodePropertiesException e) {
			System.err.println(e.getMessage());
			fail("Flag parser exception for valid command line.");
		}
		
		assertEquals("localhost", properties_.getIp());
		assertEquals(10006, properties_.getPort());
		assertEquals("01", properties_.getGroupId());
		assertTrue(properties_.getNode().toString().equals("G01"));
		
		Topology tpl = properties_.getTopology();
		assertTrue(tpl.isReachable("S01"));
		assertTrue(tpl.isReachable("S02"));

		NodeLocations nl = properties_.getServerLocations();
		NodeLocations.Location l = nl.getLocationForNode("S03");
		assertEquals(10004, l.getPort());
		assertEquals("12.13.14.15", l.getIp());
	}
	
	
	
	private void testInvalidArgument(String[] args, String expectedError) {
		properties_ = null;
		try {
			properties_ = new NodeProperties(args, false);
			fail("Invalid flag should raise exception.");
		} catch (NodeProperties.NodePropertiesException e) {
			assertNotSame(expectedError, e.getMessage());
		}
	}
	
	public void testInValidIntegerProperty() {
		String[] args = new String[2];		
		args[0] = "-id";
		args[1] = "not-an-int";
		
		testInvalidArgument(
				args,
				"Could not parse integer. For input string: \"not-an-int\"");
	}
	
	public void testInValidName() {
		String[] args = new String[2];
		args[0] = "-unknown";
		args[1] = "20";
		
		testInvalidArgument(args, "Unknown flag: unknown");
	}
	
	public void testInvalidFormat() {
		String[] args = new String[1];		
		args[0] = "-incomplete";
		
		testInvalidArgument(args, "Value not present for argument: incomplete");
	}
}