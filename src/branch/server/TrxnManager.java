package branch.server;


/**
 * 
 * Gives a processTransaction interface for processing a transaction.
 *
 */

public class TrxnManager {
	private Trxn trxn_;
	private AccDetails accounts_;

	public TrxnManager(Trxn ts, AccDetails accounts) {
		trxn_ = ts;
		accounts_ = accounts;
	}

	public TrxnResponse processTransaction() {

		/* need to set balance, status, serial no. and Error msg in response */
		TrxnResponse trxnResponse = null;
		String serial_Num = trxn_.getSerialNum();

		Double balance = 0.0;
		switch (trxn_.getType()) {
		case DEPOSIT:

			if (!TransactionLog.containsTrxn(trxn_.getSerialNum())) {
				accounts_.deposit(trxn_.getSourceAccount(), trxn_.getAmount());
				TransactionLog.addTrxn(trxn_);
			}
			balance = accounts_.query(trxn_.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn_.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case WITHDRAW:

			if (!TransactionLog.containsTrxn(serial_Num)) {
				accounts_.withdraw(trxn_.getSourceAccount(), trxn_.getAmount());
				TransactionLog.addTrxn(trxn_);
			}
			balance = accounts_.query(trxn_.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn_.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case QUERY:
			/* not added to log, not even checked in log */
			balance = accounts_.query(trxn_.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn_.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case TRANSFER:
			if(trxn_.getSourceBranch().equalsIgnoreCase(BranchServer.getProperties().getGroupId())) {
				trxnResponse = handleTransferAtSource();
			} else if (trxn_.getDestBranch().equalsIgnoreCase(BranchServer.getProperties().getGroupId())) {
				trxnResponse = handleTransferAtDest();
			} else {
				System.err.println("Incorrect transaction");
			}
			break;
		default: 
			System.err.println("Illegal state. Exiting");
			System.exit(1);
		}
		return trxnResponse;
	}

	private TrxnResponse handleTransferAtDest() {
		TrxnResponse trxnResponse;
		Double balance;
		// handle transfer at destination
		if (!TransactionLog.containsTrxn(trxn_.getSerialNum())) {
			accounts_.deposit(trxn_.getDestAccount(), trxn_.getAmount());
			TransactionLog.addTrxn(trxn_);
		}
		balance = accounts_.query(trxn_.getDestAccount());
		trxnResponse = new TrxnResponse(trxn_.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
		return trxnResponse;
	}

	private TrxnResponse handleTransferAtSource() {
		TrxnResponse trxnResponse;
		Double balance = (double) -1;
		String destinationGrp = trxn_.getDestBranch();
		
		// See if Destination Server is reachable.
		if (!trxn_.getDestBranch().equalsIgnoreCase(trxn_.getSourceBranch())) {
			final Topology topology = BranchServer.getProperties().getTopology();
			if (!topology.isReachable(destinationGrp)) {
				trxnResponse = new TrxnResponse(trxn_.getSerialNum()
						, TrxnResponse.Type.TRANSACTION 
						, accounts_.query(trxn_.getSourceAccount())
						, true
						, "Error: Destination Server Not reachable in topology.");
				return trxnResponse;
			}
		}
		// Local Withdraw 
		if (!TransactionLog.containsTrxn(trxn_.getSerialNum())) {
			accounts_.withdraw(trxn_.getSourceAccount(), trxn_.getAmount());
			if (trxn_.getSourceBranch().equalsIgnoreCase(trxn_.getDestBranch())) {
				//Local Deposit
				balance = accounts_.deposit(trxn_.getDestAccount(), trxn_.getAmount());
			}
			TransactionLog.addTrxn(trxn_);
		}
		else {
			// TODO: Bug: The first time transfer occurs, client is sent a balance.
			// If he reexecutes his transaction, he might get a difference balance.
			trxn_ = TransactionLog.getTrxn(trxn_.getSerialNum());
			balance = accounts_.query(trxn_.getSourceAccount());
		}

		// Deposit the amount to the destination account at different branch
		if (!trxn_.getSourceBranch().equalsIgnoreCase(trxn_.getDestBranch())) {
			if (BranchServer.getProperties().getState() == NodeProperties.ServerState.TAIL) {
				Message msg = new Message(BranchServer.getProperties().getNode(),
						Message.MsgType.REQ, trxn_, null);

				if (!NetworkWrapper.sendToService(msg.toString(), destinationGrp)) {
					System.err.println("Not able to send message to destination Server.");
				}
			}
		}

		trxnResponse = new TrxnResponse(trxn_.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
		return trxnResponse;
	}
}
