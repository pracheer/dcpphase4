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
		assertEquals("01", NodeName.getService("G01"));
		assertEquals("02", NodeName.getService("G02"));
		assertEquals("01", NodeName.getService("01_M01"));
		assertEquals("01", NodeName.getService("01_M02"));
		assertEquals("02", NodeName.getService("02_M01"));
	}
	
	public void testGetGui() {
		assertEquals("G01", NodeName.getGui("G01"));
		assertEquals("G02", NodeName.getGui("G02"));
		assertEquals("G01", NodeName.getGui("01_M01"));
		assertEquals("G01", NodeName.getGui("01_M02"));
		assertEquals("G02", NodeName.getGui("02_M01"));
	}
	
	public void testIsGui() {
		assertTrue(NodeName.isGui("G01"));
		assertTrue(NodeName.isGui("G02"));
		assertFalse(NodeName.isGui("01_M01"));
		assertFalse(NodeName.isGui("01_M02"));
		assertFalse(NodeName.isGui("02_M01"));
	}
	
	public void testIsServer() {
		assertFalse(NodeName.isBranchServer("G01"));
		assertFalse(NodeName.isBranchServer("G02"));
		assertFalse(NodeName.isBranchServer("01_"));
		assertFalse(NodeName.isBranchServer("01"));
		assertTrue(NodeName.isBranchServer("01_M01"));
		assertTrue(NodeName.isBranchServer("01_M02"));
		assertTrue(NodeName.isBranchServer("02_M01"));
		
	}
	
	public void testIsService() {
		assertFalse(NodeName.isBranchService("G01"));
		assertFalse(NodeName.isBranchService("G02"));
		assertFalse(NodeName.isBranchService("01_M01"));
		assertFalse(NodeName.isBranchService("01_M02"));
		assertFalse(NodeName.isBranchService("02_M01"));
		assertFalse(NodeName.isBranchService("02_"));
		assertTrue(NodeName.isBranchService("01"));
		assertTrue(NodeName.isBranchService("02"));
	}
	
	public void testGetGuiForService() {
		assertEquals("G01", NodeName.getGuiForService("01"));
		assertEquals("G02", NodeName.getGuiForService("02"));
	}
	
	public void testGetGuiForServer() {
		assertEquals("G02", NodeName.getGuiForServer("02_M01"));
		assertEquals("G01", NodeName.getGuiForServer("01_M02"));
	}
	
	public void testGetServiceForGui() {
		assertEquals("01", NodeName.getServiceForGui("G01"));
		assertEquals("02", NodeName.getServiceForGui("G02"));
	}
	
	public void testGetServiceForServer() {
		assertEquals("02", NodeName.getServiceForServer("02_M01"));
		assertEquals("01", NodeName.getServiceForServer("01_M02"));
	}
	
	public void testGetMachineForServer() {
		assertEquals("M01", NodeName.getMachineForServer("02_M01"));
		assertEquals("M02", NodeName.getMachineForServer("01_M02"));
	}
}
