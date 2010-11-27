package branch.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import branch.server.Trxn.TransxType;

public class Snapshot {

	public static enum State { 
		NOT_STARTED,
		RECORDING_CHANNELS,  // local state has been recorded, listening channels 
		LOCALLY_COMPLETE
	}

	public static final String SEPARATOR = "##";
	public static final String ACCOUNT_STR = "ACCOUNT DETAILS-";
	public static final String CHANNEL_STR = "CHANNEL DETAILS-";


	private String snapshotId_ = "";
	private State state_ = State.NOT_STARTED;
	private HashMap<String, Double> accounts_;
	private HashMap<String, ArrayList<Message>> oChannels; 	// This will keep track of channels on which markers HAVE NOT been received (open Channels).
	private HashMap<String, ArrayList<Message>> cChannels;	// This will keep track of channels on which markers HAVE been received (closed Channels).


	/* this function initiates a snapshot, stores local state 
	 * and sends out markers on all outgoing channels */
	@SuppressWarnings("unchecked")
	public Snapshot(String Id, HashMap<String, Double> accounts) {
		NodeProperties properties = BranchServer.getProperties();
		/* reading topology info */
		final Topology tpl = BranchServer.getProperties().getTopology();
		ArrayList<String> outNeighbors = tpl.getOutNeighbors();
		ArrayList<String> inNeighbors = tpl.getInNeighbors();

		/* Inititiating variables */
		this.snapshotId_ = Id;
		this.accounts_ = accounts; // local state recorded
		this.state_ = State.RECORDING_CHANNELS;
		this.oChannels = new HashMap<String, ArrayList<Message>>(inNeighbors.size());
		this.cChannels = new HashMap<String, ArrayList<Message>>(inNeighbors.size());

		/* Opening channels to record */
		for (String inNeighbor : inNeighbors) {
			oChannels.put(inNeighbor, new ArrayList<Message>());
		}

		System.out.println("\nInitiating snapshot " + snapshotId_ 
				+ "\noutgoing channels " + outNeighbors.toString()
				+ "\nincomig channels " + inNeighbors.toString());

		// TODO:sending out markers on all outgoing channels.
		Trxn newTrxn = new Trxn(this.snapshotId_, Trxn.TransxType.SNAPSHOT_MARKER,"-1", "-1", 0.0);
		Message newMsg = new Message(properties.getNode(), Message.MsgType.REQ, newTrxn, null);

		for (String outNeighbor : outNeighbors) {
			if (!NetworkWrapper.sendToService(newMsg.toString(), outNeighbor)) {
				System.err.println("Not able to send markers on outgoing channels.");
			}
		}
		
		if (oChannels.isEmpty()) {
			this.state_ = State.LOCALLY_COMPLETE;
		}
	}

	/**
	 * Will return false to indicate an error if channel has already been closed.
	 */
	public boolean addMsgToChannel(String srcNode, Message msg) {
		if(oChannels.containsKey(srcNode)) {
			ArrayList<Message> msgs = oChannels.get(srcNode);
			msgs.add(msg);
			return true;
		}
		else {
			System.err.println(srcNode + " channel has already been closed.");
		}
		return false;
	}

	/**
	 * returns 0 if snapshot has been completed. else returns 1 if there are some channels 
	 * where marker is yet to be received -1 is returned to indicate errors.
	 */
	public int closeChannel(String srcName) {
		
		if (NodeName.isGui(srcName)) {
			return 1;
		}
		if(oChannels.containsKey(srcName)) {
			if(!cChannels.containsKey(srcName)) {
				cChannels.put(srcName, oChannels.get(srcName));
				oChannels.remove(srcName);
				System.out.println("Closing channel " + srcName + "for snapshot: " + snapshotId_);
				if(oChannels.isEmpty())
					return 0;
				else
					return 1;
			} else {
				System.err.println(srcName + " has already closed the channels");
			}
		} else {
			System.err.println(srcName + " does not have an open channel");
		}
		return -1;
	}

	public boolean isChannelOpen(String srcNode) {
		if(oChannels.containsKey(srcNode) && !cChannels.containsKey(srcNode)) {
			return true;
		}
		return false;
	}

	public void updateSnapshot(Message msg) {
		Trxn trxn = msg.getTrxn();
		String msgSrc = msg.getSrcNode().toString();

		if (this.state_ == State.LOCALLY_COMPLETE) {
			System.err.println("Trying to update a complete snapshot.");
			return;
		}

		System.out.println("\nUpdating snapshot " + snapshotId_ + " with msg " + msg.toString());

		if(trxn.getType() == TransxType.SNAPSHOT_MARKER) {
			String snapshotId = msg.getTrxn().getSerialNum();
			if (!snapshotId.equals(this.snapshotId_)) {
				System.err.println("Incorrect marker:" + snapshotId_+ " updating snapshot:" + this.snapshotId_);
				return;
			}
			int val = this.closeChannel(msgSrc);
			if (val == 0) {
				// snapshot over
				this.state_ = State.LOCALLY_COMPLETE;
				System.out.println("\nSnapshot: " + snapshotId_ + " locally COMPLETED ");
			}
		} else if(isChannelOpen(msgSrc)) {
			if (trxn.getType() == TransxType.TRANSFER) {
				addMsgToChannel(msgSrc, msg);
			} else {
				System.err.println("Unexpected message received: " + msg.toString());
			}
		}
	}

	public State getState(){
		return state_;
	}

	public String print() {
		String str = "";
		str += Snapshot.ACCOUNT_STR  + SEPARATOR;
		
		Iterator<Map.Entry<String, Double>> aIt = accounts_.entrySet().iterator();
		while (aIt.hasNext()) {
			Map.Entry<String, Double> pair = (Map.Entry<String, Double>) aIt.next();
			
			String accountNum = pair.getKey();
			Double balance = pair.getValue();
			
			str += "(" + accountNum + "): " +  balance + SEPARATOR;
		}
		
		str += Snapshot.CHANNEL_STR + SEPARATOR;
		
		Iterator<Map.Entry<String, ArrayList<Message>>> cIt = cChannels.entrySet().iterator();
		while (cIt.hasNext()) {
			Map.Entry<String, ArrayList<Message>> pair = (Map.Entry<String, ArrayList<Message>>) cIt.next();
			
			String server = pair.getKey();
			ArrayList<Message> msgArray = pair.getValue();
			str += "Channel (" + server + "): = {" ;
			
			for (int i = 0; i < msgArray.size(); ++i) {
				if (i != 0) {
					str += "  ,  ";
				}
				str += msgArray.get(i).getPrettyString();
			}
			str += "}"+SEPARATOR;
		}
		System.out.println(str);
		
		return str;
	}
	
	public static String getPrettyStringFromSnapshotString(String str) {
		String pstr = str.replaceAll(SEPARATOR, "\n");

		return pstr;
	}
}
