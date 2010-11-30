package branch.server;
import java.util.ArrayList;

/**
 * 
 * Picks up a message from MsgQueue and processes it.
 * In case of a transaction request, it creates the Trxn object for it
 * and calls the processTransaction function to process the request.
 * In case of 
 */

public class MsgProcessingThread extends Thread {
	MsgQueue messages_;
	PendingMessageQueue pmessages_;
	AccDetails accounts_;
	ServerProperties properties_;
	TrxnManager tm_;
	NetworkWrapper netWrapper_;

	public MsgProcessingThread(MsgQueue messages, ServerProperties properties) {
		messages_ = messages;
		properties_ = properties;
		pmessages_ = new PendingMessageQueue();
		accounts_ = new AccDetails();
		netWrapper_ = new NetworkWrapper(properties);
		tm_ = new TrxnManager(accounts_, netWrapper_);
	}

	public void run() {
		while (true) {
			// will block if the queue is empty.
			Message msg = messages_.getMsg();

			if (msg.getType() == Message.MsgType.REQ) { // Message is a normal Message - Request.
				try {
					sleep(properties_.getSleepTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				/* Process the transaction request */
				Message responseMessage = new Message(
						properties_.getServerName(),
						Message.MsgType.RESP,
						null,
						tm_.processTransaction(msg.getTrxn()));

				// Reply the GUI if the request came from him.
				final boolean isRequestFromGui = msg.getTrxn().getSerialNum().substring(1,3).equalsIgnoreCase(properties_.getServiceId());
				ServerProperties.ServerState myState = properties_.getState();
				View myView = properties_.getMyView();
				
				if (myState == ServerProperties.ServerState.HEAD ||
						myState == ServerProperties.ServerState.MIDDLE) {
					String nextNode = myView.getSuccessor(properties_.getServerName());
					netWrapper_.sendToServer(msg.toString(), nextNode);
					pmessages_.addMessage(msg);
				} else if (myState == ServerProperties.ServerState.TAIL) {
					if(isRequestFromGui) {
						netWrapper_.sendToGui(responseMessage.toString());
					}
					processAckMessage(msg.getTrxn().getSerialNum());
				} else {
					System.err.println("Server does not have a valid state.");
				}

			} else if (msg.getType() == Message.MsgType.SPECIAL) {
				SpecialMsg sm = msg.getSpecialMsg();
				
				if (sm.getType() == SpecialMsg.Type.VIEW) {
					processViewMessage(sm.getView());
				} else if (sm.getType() == SpecialMsg.Type.SYNC) {
					processSyncMessage(sm.getSync());
				} else if (sm.getType() == SpecialMsg.Type.CHAIN_ACK) {
					processAckMessage(sm.getAckSerialNum());
				}
			}
		}
	}
	
	private void processSyncMessage(Sync sync) {
		accounts_.synchronizeAccounts(sync);
		TransactionLog.synchronizeTransactionLog(sync);		
	}
	
	private void processViewMessage(View view) {
		// If it is my group then a few special cases.
		if (view.getGroupId().equals(properties_.getServiceId())) {
			ServerProperties.ServerState myState = properties_.getState();
			String myNode = properties_.getServerName();
			
			String myCurrSuccessor = null;
			if (properties_.getMyView() != null) {
				myCurrSuccessor = properties_.getMyView().getSuccessor(myNode);
			}
			String myNewSuccessor = view.getSuccessor(myNode);

			if (myState == ServerProperties.ServerState.TAIL) {
				if (myNewSuccessor != null) {
					// I am the tail, but now I have a successor.
					// Send a SYNC message to the new tail.
					Sync sync = new Sync(
							accounts_.getAllAccnts(),
							TransactionLog.getAllTransactions());
					Message msg = new Message(myNode, new SpecialMsg(sync));
					netWrapper_.sendToServer(msg.toString(), myNewSuccessor);
					System.out.println("Sending a sync message to " + myNewSuccessor);
					System.out.println(msg);
				}
			} else if (myCurrSuccessor != null && !myCurrSuccessor.equals(myNewSuccessor)) {
				String destServer = myNewSuccessor == null ? myNode : myNewSuccessor;
				ArrayList<Message> pms = pmessages_.getMessages();
				for (int i = 0; i < pms.size(); ++i) {
					netWrapper_.sendToServer(pms.get(i).toString(), destServer);
				}

				if (myNewSuccessor == null) {
					pmessages_.clear();
				}				
			}
		}
		
		properties_.updateView(view);
	}
	
	private void processAckMessage(String serialNum) {
		pmessages_.ackMessage(serialNum);
		
		String myNode = properties_.getServerName();
		String myPredecessor = properties_.getMyView().getPredecessor(myNode);
		
		Message msg = Message.getAckMessageFromSerialNumber(myNode, serialNum);
		
		if (myPredecessor != null) {
			netWrapper_.sendToServer(msg.toString(), myPredecessor);
		}
	}
	
}
