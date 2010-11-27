package branch.server;

import java.util.ArrayList;

/**
 * A synchronized data structure for keeping the messages.
 * The main thread (BranchServer) puts messages received through addMsg()
 * The MsgProcessingThread gets a message from the Queue using getMsg()
 */

public class MsgQueue {
	ArrayList<Message> messages;
	
	int inCount = -1;
	int outCount = -1;
	
	public MsgQueue() {
		messages = new ArrayList<Message>();
	}
	
	public synchronized void addMsg(Message msg) {
		inCount ++;
		messages.add(msg);
		this.notify();
	}
	
	public synchronized Message getMsg() {
		// if no more messages are there than the one that have been read wait till they appear.
		while(outCount == inCount) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		outCount ++;
		return messages.get(outCount);
	}

}
