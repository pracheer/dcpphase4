package branch.server;
import java.util.HashMap;

/**
 * Maintains a log of the transactions.
 * @author pg298
 *
 */

public class TransactionLog {
	
	private HashMap<String, Trxn> trxnLog = new HashMap<String, Trxn>();

	public void addTrxn(Trxn transaction) {
		trxnLog.put(transaction.getSerialNum(), transaction);
	}
	
	public boolean containsTrxn(String serialNum) {
		return trxnLog.containsKey(serialNum);
	}
	
	public Trxn getTrxn(String serialNum) {
		if (containsTrxn(serialNum)) {
			return trxnLog.get(serialNum);
		}
		else 
			return null;
		
	}
	
	public HashMap<String, Trxn> getAllTransactions() {
		return (HashMap<String, Trxn>) trxnLog.clone();
	}
	
	public void synchronizeTransactionLog(Sync sync) {
		trxnLog = sync.getTransactionLogs();
	}
}
