package test;
/**
 * 
 * @author qsh2
 */

import java.util.Vector;

import junit.framework.TestCase;
import branch.server.FlagParser;

public class FlagParserTest extends TestCase {
	protected FlagParser parser_;
	protected void setUp() {
		parser_ = new FlagParser();
	}
	
	public void testArgumentDoesNotStartWithDash() {
		String[] args = new String[2];
		args[0] = "port";
		args[1] = "20";
		
		try {
			parser_.parseFlags(args);
			fail("Should have caught FlagParserException.");
		} catch (FlagParser.FlagParseException e) {
			assertEquals("port is expected to start with '-'", e.getMessage());
		}
	}
	
	public void testNull() {
		String str1 = null;
		String str2 = "what";
		
		assertFalse(str2.equals(str1));
		
	}
		
	public void testValueNotPresent() {
		String[] args = new String[3];
		args[0] = "-port";
		args[1] = "20";
		args[2] = "-id";
		
		try {
			parser_.parseFlags(args);
			fail("Should have caught FlagParserException.");
		} catch (FlagParser.FlagParseException e) {
			assertEquals("Value not present for argument: id", e.getMessage());
		}
	}
	
	public void testValidArguments() {
		String[] args = new String[6];
		args[0] = "-port";
		args[1] = "2000";
		args[2] = "-id";
		args[3] = "20";
		args[4] = "-topology";
		args[5] = "c:\toplogyfile.txt";
		
		try {
			Vector<FlagParser.Argument> parsedArguments = parser_.parseFlags(args);
			
			assertEquals(3, parsedArguments.size());
			assertEquals("port", parsedArguments.get(0).getName());
			assertEquals("2000", parsedArguments.get(0).getValue());
			assertEquals("id", parsedArguments.get(1).getName());
			assertEquals("20", parsedArguments.get(1).getValue());
			assertEquals("topology", parsedArguments.get(2).getName());
			assertEquals("c:\toplogyfile.txt", parsedArguments.get(2).getValue());
		} catch (FlagParser.FlagParseException e) {
			fail("FlagParseException occurred in a valid test case");
		}
	}
}