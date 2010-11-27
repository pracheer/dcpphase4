package test;

import branch.server.View;
import junit.framework.TestCase;

public class ViewTest extends TestCase {

	View view;
	String str;
	public ViewTest() {
//		view = new View(1, new Node);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		view = new View("01");
		view.addServer("S01_01");
		view.addServer("S01_02");
		view.addServer("S01_03");
		view.addServer("S01_04");
		str = "01::S01_01::S01_02::S01_03::S01_04";
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		view = null;
	}

	public void testAddServer() {
		view.addServer("S01_05");
		assertEquals(str + "::S01_05", view.toString());
	}

	public void testRemoveServer() {
		
		// tail node
		assertTrue(view.removeServer("S01_04"));
		assertFalse(view.removeServer("S01_04"));
		
		// middle node
		assertTrue(view.removeServer("S01_02"));
		assertFalse(view.removeServer("S01_02"));
		
		// head node
		assertTrue(view.removeServer("S01_01"));
		assertFalse(view.removeServer("S01_01"));
		
	}

	public void testGetHead() {
		assertEquals("S01_01", view.getHead());
	}

	public void testGetTail() {
		assertEquals("S01_04", view.getTail());
	}

	public void testGetPredecessor() {
		assertEquals(null, view.getPredecessor("S01_01"));
		assertEquals("S01_01", view.getPredecessor("S01_02"));
	}

	public void testGetSuccessor() {
		assertEquals("S01_02", view.getSuccessor("S01_01"));
		assertEquals(null, view.getSuccessor("S01_04"));
	}
	
	public void testParseString() {
		view = View.parseString(str);
		assertEquals(str, view.toString());
	}
	
}
