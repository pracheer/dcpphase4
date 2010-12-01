package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import branch.server.MachineProperties;
import branch.server.ServerProperties;

public class FailureDetectionSuiteStressTest {

	private ServerProperties properties_;
	private MachineProperties machineProp_;
	private File tempServerLocationFile_;
	private File tempTopologyFile_;
	private File tempServiceConfigFile_;
	
	public static void main(String[] args) {
		
	}

	private void createProperties() throws IOException {
		tempServerLocationFile_ = File.createTempFile("locations", ".txt");
		String str;
		
		str = "";
		str += "R01_M01 localhost 10001\n";
		str += "F01_M01 localhost 10002\n";
		str += "R01_M02 localhost 10003\n";
		str += "F01_M02 localhost 10004\n";
		str += "R01_M03 localhost 10005\n";
		str += "F01_M03 localhost 10006\n";
		str += "R01_M04 localhost 10007\n";
		str += "F01_M04 localhost 10008\n";
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
		str += "M01 M04\n";
		str += "M02 M03\n";
		str += "M02 M04\n";
		str += "M03 M04\n";

		try {
			FileWriter fw = new FileWriter(tempTopologyFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		tempServiceConfigFile_ = File.createTempFile("config", ".txt");
		
		str = "";
		str += "F01:F01_M01 F01_M02 F01_M03 F01_M04\n";
		try {
			FileWriter fw = new FileWriter(tempServiceConfigFile_);
			fw.write(str.toCharArray());
			fw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		properties_ = null;
	}
}
