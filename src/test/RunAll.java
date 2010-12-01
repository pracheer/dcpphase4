package test;

import java.io.IOException;

public class RunAll {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Runtime runtime = Runtime.getRuntime();

		try {
			String args1 = "-id 01 -topology C://topology.txt -servers C://servers.txt";
			runtime.exec("java branch.server.BranchServer " +args1);
			String args2 = "-id 02 -topology C://topology.txt -servers C://servers.txt";
			runtime.exec("java branch.server.BranchServer " +args2);
			runtime.exec("java branch.server.BranchGUI " +args1);
			runtime.exec("java branch.server.BranchGUI " +args2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
