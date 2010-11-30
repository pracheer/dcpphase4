package branch.server;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A synchronized data structure for keeping the messages.
 * The main thread (BranchServer) puts messages received through addMsg()
 * The MsgProcessingThread gets a message from the Queue using getMsg()
 */

public class MsgQueue {
	Queue<Message> messages;
	
	public MsgQueue() {
		messages = new LinkedList<Message>();
	}
	
	public synchronized void addMsg(Message msg) {
		messages.add(msg);
		this.notify();
	}
	
	public synchronized Message getMsg() {
		// if no more messages are there than the one that have been read wait till they appear.
		while (messages.size() == 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return messages.remove();
	}
}
