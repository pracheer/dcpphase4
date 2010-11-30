package branch.server;


/**
 * 
 * Gives a processTransaction interface for processing a transaction.
 *
 */

public class TrxnManager {
	private AccDetails accounts_;
	private ServerProperties properties_;
	private NetworkWrapper netWrapper_;

	public TrxnManager(AccDetails accounts, NetworkWrapper nw) {
		accounts_ = accounts;
		netWrapper_ = nw;
		properties_ = netWrapper_.getServerProperties();
	}

	public TrxnResponse processTransaction(Trxn trxn) {
		/* need to set balance, status, serial no. and Error msg in response */
		TrxnResponse trxnResponse = null;
		String serial_Num = trxn.getSerialNum();

		Double balance = 0.0;
		switch (trxn.getType()) {
		case DEPOSIT:

			if (!TransactionLog.containsTrxn(trxn.getSerialNum())) {
				accounts_.deposit(trxn.getSourceAccount(), trxn.getAmount());
				TransactionLog.addTrxn(trxn);
			}
			balance = accounts_.query(trxn.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case WITHDRAW:

			if (!TransactionLog.containsTrxn(serial_Num)) {
				accounts_.withdraw(trxn.getSourceAccount(), trxn.getAmount());
				TransactionLog.addTrxn(trxn);
			}
			balance = accounts_.query(trxn.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case QUERY:
			/* not added to log, not even checked in log */
			balance = accounts_.query(trxn.getSourceAccount());
			trxnResponse = new TrxnResponse(trxn.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
			break;

		case TRANSFER:
			if(trxn.getSourceBranch().equalsIgnoreCase(properties_.getServiceId())) {
				trxnResponse = handleTransferAtSource(trxn);
			} else if (trxn.getDestBranch().equalsIgnoreCase(properties_.getServiceId())) {
				trxnResponse = handleTransferAtDest(trxn);
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

	private TrxnResponse handleTransferAtDest(Trxn trxn) {
		TrxnResponse trxnResponse;
		Double balance;
		// handle transfer at destination
		if (!TransactionLog.containsTrxn(trxn.getSerialNum())) {
			accounts_.deposit(trxn.getDestAccount(), trxn.getAmount());
			TransactionLog.addTrxn(trxn);
		}
		balance = accounts_.query(trxn.getDestAccount());
		trxnResponse = new TrxnResponse(trxn.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
		return trxnResponse;
	}

	private TrxnResponse handleTransferAtSource(Trxn trxn) {
		TrxnResponse trxnResponse;
		Double balance = (double) -1;
		String destService = trxn.getDestBranch();
		
		// See if Destination Server is reachable.
		if (!trxn.getDestBranch().equalsIgnoreCase(trxn.getSourceBranch())) {
			if (!properties_.isServiceReachable(destService)) {
				trxnResponse = new TrxnResponse(trxn.getSerialNum()
						, TrxnResponse.Type.TRANSACTION 
						, accounts_.query(trxn.getSourceAccount())
						, true
						, "Error: Destination Server Not reachable in topology.");
				return trxnResponse;
			}
		}
		// Local Withdraw 
		if (!TransactionLog.containsTrxn(trxn.getSerialNum())) {
			balance = accounts_.withdraw(trxn.getSourceAccount(), trxn.getAmount());
			if (trxn.getSourceBranch().equalsIgnoreCase(trxn.getDestBranch())) {
				//Local Deposit
				accounts_.deposit(trxn.getDestAccount(), trxn.getAmount());
			}
			TransactionLog.addTrxn(trxn);
		}
		else {
			trxn = TransactionLog.getTrxn(trxn.getSerialNum());
			balance = accounts_.query(trxn.getSourceAccount());
		}

		// Deposit the amount to the destination account at different branch
		if (!trxn.getSourceBranch().equalsIgnoreCase(trxn.getDestBranch())) {
			if (properties_.getState() == ServerProperties.ServerState.TAIL) {
				Message msg = new Message(properties_.getServerName(),
						Message.MsgType.REQ, trxn, null);

				if (!netWrapper_.sendToService(msg.toString(), destService)) {
					System.err.println("Not able to send message to destination Server.");
				}
			}
		}

		trxnResponse = new TrxnResponse(trxn.getSerialNum(), TrxnResponse.Type.TRANSACTION, balance, false, "");
		return trxnResponse;
	}
}
