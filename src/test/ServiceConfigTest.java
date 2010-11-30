package test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import branch.server.ServiceConfig;
import branch.server.View;

import junit.framework.TestCase;

public class ServiceConfigTest extends TestCase {
	private File tempFile_;
	
	protected void setUp() throws Exception {
		tempFile_ = File.createTempFile("config", ".txt");
		
		String str = "";
		str += "# Comment line. Ignore.\n";
		str += "S01: S01_M01 S01_M02\n";
		str += "S02: S02_M02 S02_M01\n";
		str += "S03: S03_M03\n";
		str += "S04: S04_M04 S04_M05 S04_M06\n";
		str += "S05: S05_M05 S05_M04 S05_M06\n";
		str += "S06: S06_M05 S06_M04 S06_M06\n";
		
		str += "err err (should ignore)\n";
		
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
	
	protected ServiceConfig createConfigFromFile() {
		ServiceConfig sc = null;
		try {
			sc = new ServiceConfig(tempFile_.getAbsolutePath());
			sc.parseConfigFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		return sc;
	}
	
	public void testView() {
		ServiceConfig sc = createConfigFromFile();
		
		View v = sc.getView("S01");
		assertEquals("S01::S01_M01::S01_M02", v.toString());
		v = sc.getView("S02");
		assertEquals("S02::S02_M02::S02_M01", v.toString());
		v = sc.getView("S03");
		assertEquals("S03::S03_M03", v.toString());
		v = sc.getView("S04");
		assertEquals("S04::S04_M04::S04_M05::S04_M06", v.toString());
		v = sc.getView("S05");
		assertEquals("S05::S05_M05::S05_M04::S05_M06", v.toString());
		v = sc.getView("S06");
		assertEquals("S06::S06_M05::S06_M04::S06_M06", v.toString());
	}
	
	public void testViewUpdate() {
		ServiceConfig sc = createConfigFromFile();
		
		View v = View.parseString("S01::S01_M01::S01_M02::S01_M03");
		sc.updateView(v);

		assertEquals("S01::S01_M01::S01_M02::S01_M03", sc.getView("S01").toString());
	}
	
}
