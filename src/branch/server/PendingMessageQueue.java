package branch.server;

import java.util.ArrayList;

public class PendingMessageQueue {
	ArrayList<Message> messages_;
	
	public PendingMessageQueue() {
		messages_ = new ArrayList<Message>();
	}

	public void addMessage(Message msg) {
		messages_.add(msg);
	}

	public void ackMessage(String serialNum) {
		for (int i = 0; i < messages_.size(); ++i) {
			Message msg = messages_.get(i);
			
			if (msg.getType() == Message.MsgType.REQ) {
				if (serialNum.equals(msg.getTrxn().getSerialNum())) {
					messages_.remove(i);
					break;
				}
			}			
		}
	}
	
	public ArrayList<Message> getMessages() {
		return messages_;
	}
	
	public void clear() {
		messages_.clear();
	}
}
