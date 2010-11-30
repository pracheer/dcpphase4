package test;

import branch.server.*;

import junit.framework.TestCase;

/**
 * 
 * @author qsh2
 *
 */

public class NodeNameTest extends TestCase {
	public void testGetService() {
		assertEquals("S01", NodeName.getService("G01"));
		assertEquals("S02", NodeName.getService("G02"));
		assertEquals("S01", NodeName.getService("S01_M01"));
		assertEquals("S01", NodeName.getService("S01_M02"));
		assertEquals("S02", NodeName.getService("S02_M01"));
	}
	
	public void testGetGui() {
		assertEquals("G01", NodeName.getGui("G01"));
		assertEquals("G02", NodeName.getGui("G02"));
		assertEquals("G01", NodeName.getGui("S01_M01"));
		assertEquals("G01", NodeName.getGui("S01_M02"));
		assertEquals("G02", NodeName.getGui("S02_M01"));
	}
	
	public void testIsGui() {
		assertTrue(NodeName.isGui("G01"));
		assertTrue(NodeName.isGui("G02"));
		assertFalse(NodeName.isGui("S01_M01"));
		assertFalse(NodeName.isGui("S01_M02"));
		assertFalse(NodeName.isGui("S02_M01"));
	}
	
	public void testIsServer() {
		assertFalse(NodeName.isServer("G01"));
		assertFalse(NodeName.isServer("G02"));
		assertFalse(NodeName.isServer("S01_"));
		assertFalse(NodeName.isServer("S01"));
		assertTrue(NodeName.isServer("S01_M01"));
		assertTrue(NodeName.isServer("S01_M02"));
		assertTrue(NodeName.isServer("S02_M01"));
		
	}
	
	public void testIsService() {
		assertFalse(NodeName.isService("G01"));
		assertFalse(NodeName.isService("G02"));
		assertFalse(NodeName.isService("S01_M01"));
		assertFalse(NodeName.isService("S01_M02"));
		assertFalse(NodeName.isService("S02_M01"));
		assertFalse(NodeName.isService("S02_"));
		assertTrue(NodeName.isService("S01"));
		assertTrue(NodeName.isService("S02"));
	}
	
	public void testGetGuiForService() {
		assertEquals("G01", NodeName.getGuiForService("S01"));
		assertEquals("G02", NodeName.getGuiForService("S02"));
	}
	
	public void testGetGuiForServer() {
		assertEquals("G02", NodeName.getGuiForServer("S02_M01"));
		assertEquals("G01", NodeName.getGuiForServer("S01_M02"));
	}
	
	public void testGetServiceForGui() {
		assertEquals("S01", NodeName.getServiceForGui("G01"));
		assertEquals("S02", NodeName.getServiceForGui("G02"));
	}
	
	public void testGetServiceForServer() {
		assertEquals("S02", NodeName.getServiceForServer("S02_M01"));
		assertEquals("S01", NodeName.getServiceForServer("S01_M02"));
	}
	
	public void testGetMachineForServer() {
		assertEquals("M01", NodeName.getMachineForServer("S02_M01"));
		assertEquals("M02", NodeName.getMachineForServer("S01_M02"));
	}
}
