package test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import branch.server.Topology;

import junit.framework.TestCase;

public class TopologyTest extends TestCase {
	private File tempFile_;
	
	protected void setUp() throws Exception {
		tempFile_ = File.createTempFile("topology", ".txt");
		
		String str = "";
		str += "M01 M05\n";
		str += "M02 M01\n";
		str += "err err (topology file should ignore)\n";
		
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
	
	protected Topology createTopologyFile(String filePath) {
		Topology tpl = null;
		try {
			tpl = new Topology(tempFile_.getAbsolutePath());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		return tpl;
	}

	public void testIsMachineReachable() {
		Topology tpl = createTopologyFile(tempFile_.getAbsolutePath());
		assertTrue(tpl.isMachineReachable("M01", "M01"));
		assertTrue(tpl.isMachineReachable("M02", "M02"));
		assertTrue(tpl.isMachineReachable("M01", "M02"));
		assertTrue(tpl.isMachineReachable("M02", "M01"));
		assertTrue(tpl.isMachineReachable("M01", "M05"));
		assertTrue(tpl.isMachineReachable("M05", "M01"));
		assertFalse(tpl.isMachineReachable("M02", "M05"));
	}
	
	
	public void testIsServerReachable() {
		Topology tpl = createTopologyFile(tempFile_.getAbsolutePath());
		assertTrue(tpl.isServerReachable("01_M01", "01_M01"));
		assertTrue(tpl.isServerReachable("01_M02", "02_M02"));
		assertTrue(tpl.isServerReachable("01_M01", "01_M02"));
		assertTrue(tpl.isServerReachable("02_M02", "02_M01"));
		assertTrue(tpl.isServerReachable("01_M01", "02_M02"));
		assertTrue(tpl.isServerReachable("02_M02", "01_M01"));
		assertTrue(tpl.isServerReachable("03_M01", "11_M05"));
		assertTrue(tpl.isServerReachable("12_M05", "10_M01"));
		assertFalse(tpl.isServerReachable("01_M02", "01_M05"));
		assertFalse(tpl.isServerReachable("01_M02", "02_M05"));
		assertFalse(tpl.isServerReachable("03_M02", "11_M05"));
		
		// GUI specific.
		assertTrue(tpl.isServerReachable("01_M11", "G01"));
		assertTrue(tpl.isServerReachable("02_M05", "G02"));
		assertTrue(tpl.isServerReachable("G02", "02_M05"));
		assertFalse(tpl.isServerReachable("G02", "G01"));
		assertFalse(tpl.isServerReachable("G01", "02_M01"));
		assertFalse(tpl.isServerReachable("G02", "01_M05"));
	}
	
	public void testTopologyCreation() {
		Topology tpl = null;
		try {
			tpl = new Topology("no-file");
			fail("Creating topology from bad file should raise exception.");
		} catch(IOException e) {
			// test passes.
		}
	}
}
