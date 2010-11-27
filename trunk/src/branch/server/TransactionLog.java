package branch.server;
import java.util.HashMap;

/**
 * Maintains a log of the transactions.
 * @author pg298
 *
 */

public class TransactionLog {
	
	private static HashMap<String, Trxn> trxnLog = new HashMap<String, Trxn>();

	public static void addTrxn(Trxn transaction) {
		trxnLog.put(transaction.getSerialNum(), transaction);
	}
	
	public static boolean containsTrxn(String serialNum) {
		return trxnLog.containsKey(serialNum);
	}
	
	public static Trxn getTrxn(String serialNum) {
		if (containsTrxn(serialNum)) {
			return trxnLog.get(serialNum);
		}
		else 
			return null;
		
	}
	
	public static HashMap<String, Trxn> getAllTransactions() {
		return (HashMap<String, Trxn>) trxnLog.clone();
	}
	
	public static void synchronizeTransactionLog(Sync sync) {
		trxnLog = sync.getTransactionLogs();
	}
}
