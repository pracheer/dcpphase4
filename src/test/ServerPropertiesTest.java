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
import branch.server.ServerProperties;
import branch.server.MachineProperties;
import branch.server.Topology;
import junit.framework.TestCase;

public class ServerPropertiesTest extends TestCase {
	private ServerProperties properties_;
	private MachineProperties machineProp_;
	private File tempServerLocationFile_;
	private File tempTopologyFile_;
	private File tempServiceConfigFile_;
	
	public void setUp() throws Exception {
		tempServerLocationFile_ = File.createTempFile("locations", ".txt");
		String str;
		
		str = "";
		str += "S01_M01 localhost 10001\n";
		str += "S02_M01 11.12.13.14 10002\n";
		str += "S03_M02 12.13.14.15 10004\n";
		str += "S04_M03 localhost 10005\n";
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
		str += "M01 M02\n";
		str += "M01 M03\n";
		
		try {
			FileWriter fw = new FileWriter(tempTopologyFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		tempServiceConfigFile_ = File.createTempFile("config", ".txt");
		
		str = "";
		str += "S01: S01_M01\n";
		str += "S02: S02_M01\n";
		str += "S03: S03_M02\n";
		str += "S04: S04_M03\n";
		
		try {
			FileWriter fw = new FileWriter(tempServiceConfigFile_);
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
		args[1] = "M02";
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
				"S03_M02",
				false);

		assertEquals(10004, properties_.getPort());
		assertEquals("12.13.14.15", properties_.getIp());
		assertEquals("S03", properties_.getServiceId());
		assertTrue(properties_.getServerName().equals("S03_M02"));
		
		Topology tpl = properties_.getTopology();
		assertTrue(tpl.isServerReachable("S03_M02", "G03"));
		assertTrue(tpl.isServerReachable("G03", "S03_M02"));
		
		assertTrue(properties_.isServiceReachable("S01"));
		assertTrue(properties_.isServiceReachable("S02"));
		assertTrue(properties_.isServiceReachable("S03"));
		assertFalse(properties_.isServiceReachable("S04"));

		NodeLocations nl = properties_.getServerLocations();
		NodeLocations.Location l = nl.getLocationForNode("S01_M01");
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
				"G01",
				true);
		
		assertEquals("localhost", properties_.getIp());
		assertEquals(10006, properties_.getPort());
		assertEquals("S01", properties_.getServiceId());
		assertTrue(properties_.getServerName().equals("G01"));
		
		Topology tpl = properties_.getTopology();
		assertTrue(tpl.isServerReachable("G01", "S01_M01"));
		assertFalse(tpl.isServerReachable("G01", "S02_M01"));
		
		assertTrue(properties_.isServiceReachable("S01"));
		assertFalse(properties_.isServiceReachable("S02"));
		assertFalse(properties_.isServiceReachable("S03"));
		assertFalse(properties_.isServiceReachable("S04"));

		NodeLocations nl = properties_.getServerLocations();
		NodeLocations.Location l = nl.getLocationForNode("S03_M02");
		assertEquals(10004, l.getPort());
		assertEquals("12.13.14.15", l.getIp());
	}
	
	
	
	private void testInvalidArgument(String[] args, String expectedError) {
		machineProp_ = null;
		try {
			machineProp_ = new MachineProperties(args);
			fail("Invalid flag should raise exception.");
		} catch (MachineProperties.PropertiesException e) {
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