package branch.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * 
 * Picks up a message from MsgQueue and processes it.
 * It also implements the calling of different snapshot mechanism.
 * It contains the reference of the snapshot objects being processed.
 * If a new SNAPSHOT_MARKER comes it updates the currently running snapshot states
 * by stopping recording in an incoming channel or by creating a new snapshot process.
 * In case of a transaction request, it creates the Trxn object for it
 * and calls the processTransaction function to process the request.
 * It will also update the incoming-channel states for the pending snapshots if
 * required.
 */

public class MsgProcessingThread extends Thread {
	MsgQueue messages_;
	PendingMessageQueue pmessages_;
	AccDetails accounts_;

	public MsgProcessingThread(MsgQueue messages) {
		messages_ = messages;
		pmessages_ = new PendingMessageQueue();
		accounts_ = new AccDetails();
	}

	public void run() {
		NodeProperties properties = BranchServer.getProperties();

		while (true) {
			// will block if the queue is empty.
			Message msg = messages_.getMsg();

//			if(msg.getType() == Message.MsgType.REQ && msg.getTrxn().getType() == Trxn.TransxType.SNAPSHOT_MARKER) {
//				String snapshotId = msg.getTrxn().getSerialNum();
//				Snapshot snapshot;
//
//				// Thread goes to sleep as per the properties specified.
//				// Useful to test snapshots.
//				if(!snapshots.containsKey(snapshotId)) {
//					try {
//						sleep(properties.getSleepTime());
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				if (!snapshots.containsKey(snapshotId)) {
//					/* initiating a new snapshot */
//					snapshot = new Snapshot(snapshotId, (HashMap<String, Double>) accounts_.getAllAccnts().clone());
//					snapshots.put(snapshotId, snapshot);			
//				}
//
//				snapshot = snapshots.get(snapshotId);
//				snapshot.updateSnapshot(msg);
//				
//				if(snapshot.getState() == Snapshot.State.LOCALLY_COMPLETE){
//					// Send the completed snapshot response to the GUI for display.
//					System.out.println(snapshot.print());
//					Message responseMessage = new Message(
//							properties.getNode(),
//							Message.MsgType.RESP,
//							null,
//							new TrxnResponse(snapshotId, TrxnResponse.Type.SNAPSHOT, snapshot.print()));
//					NetworkWrapper.sendToGui(responseMessage.toString());
//				}
//			} else 
			if (msg.getType() == Message.MsgType.REQ) { // Message is a normal Message - Request.
				try {
					sleep(properties.getSleepTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				/* Process the transaction request */
				TrxnManager tm = new TrxnManager(msg.getTrxn(), accounts_);
				Message responseMessage = new Message(
						properties.getNode(),
						Message.MsgType.RESP,
						null,
						tm.processTransaction());

				// Reply the GUI if the request came from him.
				final boolean isRequestFromGui = msg.getTrxn().getSerialNum().substring(1,3).equalsIgnoreCase(properties.getGroupId());
				NodeProperties.ServerState myState = properties.getState();
				View myView = properties.getMyView();
				
				if (myState == NodeProperties.ServerState.HEAD ||
						myState == NodeProperties.ServerState.MIDDLE) {
					String nextNode = myView.getSuccessor(properties.getNode());
					NetworkWrapper.sendToServer(msg.toString(), nextNode);
					pmessages_.addMessage(msg);
				} else if (myState == NodeProperties.ServerState.TAIL) {
					if(isRequestFromGui) {
						NetworkWrapper.sendToGui(responseMessage.toString());
					}
					processAckMessage(msg.getTrxn().getSerialNum());
				} else {
					System.err.println("Server does not have a valid state.");
				}

				// update all snapshots with this msg
//				if (!snapshots.isEmpty()) {
//					for (Snapshot sn : snapshots.values()) {
//						if (sn.getState() == Snapshot.State.RECORDING_CHANNELS) {
//							sn.updateSnapshot(msg);
//						}
//					}
//				}
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
		NodeProperties properties = BranchServer.getProperties();
		
		// If it is my group then a few special cases.
		if (view.getGroupId().equals(properties.getGroupId())) {
			NodeProperties.ServerState myState = properties.getState();
			String myNode = properties.getNode();
			
			String myCurrSuccessor = null;
			if (properties.getMyView() != null) {
				myCurrSuccessor = properties.getMyView().getSuccessor(myNode);
			}
			String myNewSuccessor = view.getSuccessor(myNode);

			if (myState == NodeProperties.ServerState.TAIL) {
				if (myNewSuccessor != null) {
					// I am the tail, but now I have a successor.
					// Send a SYNC message to the new tail.
					Sync sync = new Sync(
							accounts_.getAllAccnts(),
							TransactionLog.getAllTransactions());
					Message msg = new Message(myNode, new SpecialMsg(sync));
					NetworkWrapper.sendToServer(msg.toString(), myNewSuccessor);
					System.out.println("Sending a sync message to " + myNewSuccessor);
					System.out.println(msg);
				}
			} else if (myCurrSuccessor != null && !myCurrSuccessor.equals(myNewSuccessor)) {
				String destServer = myNewSuccessor == null ? myNode : myNewSuccessor;
				ArrayList<Message> pms = pmessages_.getMessages();
				for (int i = 0; i < pms.size(); ++i) {
					NetworkWrapper.sendToServer(pms.get(i).toString(), destServer);
				}

				if (myNewSuccessor == null) {
					pmessages_.clear();
				}				
			}
		}
		
		properties.updateView(view);
	}
	
	private void processAckMessage(String serialNum) {
		pmessages_.ackMessage(serialNum);
		
		String myNode = BranchServer.getProperties().getNode();
		String myPredecessor = BranchServer.getProperties().getMyView().getPredecessor(myNode);
		
		Message msg = Message.getAckMessageFromSerialNumber(myNode, serialNum);
		
		if (myPredecessor != null) {
			NetworkWrapper.sendToServer(msg.toString(), myPredecessor);
		}
	}
	
}
