package branch.server;

import java.util.Vector;

public class Suspects {
	
	private static final String SUSPECTS = "SUSPECTS";
	private static String msgSeparator = "::";


	String nodeName;
	Vector<String> suspects = new Vector<String>();

	public Suspects(String nodeName, Vector<String> suspects) {
		super();
		this.nodeName = nodeName;
		this.suspects = suspects;
	}

	public String toString() {
		String msg = SUSPECTS + msgSeparator + nodeName;
		for (String suspect : suspects) {
			msg += msgSeparator + suspect;
		}

		return msg;
	}

	public static Suspects parseSuspectsString(String msg) {
		String[] strings = msg.split(msgSeparator);
		if(!strings[0].equals(SUSPECTS)) {
			System.err.println("message format invalide");
			return null;
		}
			
		Vector<String> s = new Vector<String>();
		for(int i = 2; i < strings.length; i++)
			s.add(strings[i]);

		return new Suspects(strings[1], s);
	}

	public String getNodeName() {
		return nodeName;
	}

	public Vector<String> getSuspects() {
		return suspects;
	}
	
}
