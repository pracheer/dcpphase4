package test;

import java.util.Vector;

import branch.server.FDServer;

import junit.framework.TestCase;

public class FDServerTest extends TestCase {
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
		
		FDServer.getMachinesToChange(prvs, news, toadd, torem);
		
		assertEquals("M05", toadd.get(0));
		assertEquals("M06", toadd.get(1));
		
		assertEquals("M02", torem.get(0));
		assertEquals("M08", torem.get(1));
		assertEquals("M01", torem.get(2));
		
		System.out.println("passed");
	}
}
