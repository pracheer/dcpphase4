package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import branch.server.FDServer;
import branch.server.NodeLocations;
import branch.server.ServiceConfig;
import branch.server.View;

import junit.framework.TestCase;

public class FDServerTest extends TestCase {
	/*
	public void testMachinesToChange() {
		Vector<String> prvs = new Vector<String>(), news = new Vector<String>();
		Vector<String> toadd = new Vector<String>(), torem = new Vector<String>();
		
		prvs.add("M02");
		prvs.add("M03");
		prvs.add("M04");
		prvs.add("M08");
		
		
		prvs.add("M01");
		news.add("M03");
		news.add("M04");
		news.add("M05");
		news.add("M06");
		
//		FDServer.getMachinesToChange(prvs, news, toadd, torem);
		
		assertEquals("M05", toadd.get(0));
		assertEquals("M06", toadd.get(1));
		
		assertEquals("M02", torem.get(0));
		assertEquals("M08", torem.get(1));
		assertEquals("M01", torem.get(2));
	}
	*/
	
	public void testViewsToUpdate() {
		Vector<String> toAdd = new Vector<String>();
		Vector<String> toRem = new Vector<String>();
		
		toAdd.add("M02");
		toAdd.add("M03");

		toRem.add("M05");
		
		ServiceConfig sc = new ServiceConfig("");
		
		View v1 = new View("01");
		v1.addServer("01_M01");
		sc.updateView(v1);
		
		View v2 = new View("02");
		v2.addServer("02_M01");
		v2.addServer("02_M03");
		sc.updateView(v2);
		
		View v3 = new View("03");
		v3.addServer("03_M04");
		v3.addServer("03_M05");
		sc.updateView(v3);

		View v4 = new View("04");
		v4.addServer("04_M01");
		v4.addServer("04_M04");
		sc.updateView(v4);

		View v5 = new View("05");
		v5.addServer("05_M01");
		v5.addServer("05_M04");
		v5.addServer("05_M05");
		sc.updateView(v5);
		
		View v6 = new View("F05");
		v6.addServer("F05_M01");
		v6.addServer("F05_M05");
		sc.updateView(v6);
		
		
		File tempFile = null;
		String str = "";
		str += "01_M01 localhost 10001\n";
		str += "02_M01 localhost 10002\n";
		str += "02_M02 localhost 10003\n";
		str += "02_M03 localhost 10004\n";
		str += "03_M02 localhost 10005\n";
		str += "03_M03 localhost 10006\n";
		str += "03_M04 localhost 10007\n";
		str += "03_M05 localhost 10008\n";
		str += "04_M01 localhost 10009\n";
		str += "04_M04 localhost 10010\n";
		str += "05_M01 localhost 10011\n";
		str += "05_M02 localhost 10012\n";
		str += "05_M03 localhost 10013\n";
		str += "05_M04 localhost 10014\n";
		str += "05_M05 localhost 10015\n";		
		str += "F05_M01 localhost 10016\n";
		str += "F05_M02 localhost 10017\n";
		str += "F05_M03 localhost 10018\n";
		str += "F05_M04 localhost 10019\n";
		str += "F05_M05 localhost 10020\n";
		try {
			tempFile = File.createTempFile("locations", ".txt");
			FileWriter fw = new FileWriter(tempFile);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		NodeLocations locs = null;
		try {
			locs = new NodeLocations(tempFile.getAbsolutePath());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Vector<View> views = FDServer.getViewsToUpdate(toAdd, toRem, sc, locs);
		
		assertEquals(3, views.size());
		assertEquals("05::05_M01::05_M04::05_M02::05_M03", views.get(0).toString());
		assertEquals("03::03_M04::03_M02::03_M03", views.get(1).toString());
		assertEquals("02::02_M01::02_M03::02_M02", views.get(2).toString());
		assertEquals("F05::F05_M01::F05_M02::F05_M03", v6.toString());
	}
}
