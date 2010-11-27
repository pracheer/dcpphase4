package test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import branch.server.Topology;

import junit.framework.TestCase;

public class TopologyTest extends TestCase {
	private File tempFile_;
	
	protected void setUp() throws Exception {
		tempFile_ = File.createTempFile("topology", ".txt");
		
		String str = "";
		str += "01 05\n";
		str += "02 01\n";
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
	
	protected Topology createTopologyFile(String filePath, String node, String group) {
		Topology tpl = null;
		try {
			tpl = new Topology(tempFile_.getAbsolutePath(), group);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		return tpl;
	}

	public void testIsReachable() {
		

		Topology tpl = createTopologyFile(tempFile_.getAbsolutePath(), "S01_1", "01");
		assertTrue(tpl.isReachable("G01"));
		assertTrue(tpl.isReachable("S05_01"));

		tpl = createTopologyFile(tempFile_.getAbsolutePath(), "G01", "01");
		assertTrue(tpl.isReachable("S01_01"));
		assertTrue(tpl.isReachable("S02_01"));
		
		tpl = createTopologyFile(tempFile_.getAbsolutePath(), "S05_01", "05");		
		assertTrue(tpl.isReachable("S05_02"));
		assertTrue(tpl.isReachable("S01"));
		assertFalse(tpl.isReachable("S03"));
		assertFalse(tpl.isReachable("G02"));
		
		tpl = createTopologyFile(tempFile_.getAbsolutePath(), "G02", "02");
		assertTrue(tpl.isReachable("S02_01"));
	}
	
	public void testTopologyCreation() {
		Topology tpl = null;
		try {
			tpl = new Topology("no-file", null);
			fail("Creating topology from bad file should raise exception.");
		} catch(IOException e) {
			assertEquals(
					"no-file (The system cannot find the file specified)",
					e.getMessage());
		}
	}
	
}
