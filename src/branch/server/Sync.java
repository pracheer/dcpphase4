package branch.server;

import java.util.HashMap;
import java.util.Set;

public class Sync {

	private static String msgSeparator = ":;:";
	private HashMap<String, Double> accountDetails_;
	private HashMap<String, Trxn> trxnLog_;
	
	public Sync(
			HashMap<String, Double> accountDetails,
			HashMap<String, Trxn> trxnLog) {
		accountDetails_ = accountDetails;
		trxnLog_ = trxnLog;
	}
	
	public String toString() {
		String str = "";
		
		str += accountDetails_.size();
		Set<String> keys = accountDetails_.keySet();
		for (String key : keys) {
			str += msgSeparator + key + msgSeparator + accountDetails_.get(key);
		}
		
		str += msgSeparator + trxnLog_.size();
		keys = trxnLog_.keySet();
		for (String key : keys) {
			str += msgSeparator + key + msgSeparator + trxnLog_.get(key);
		}
		
		return str;
	}
	
	public static Sync parseString(String str) {
		
		String[] parts = str.split(msgSeparator);
		
		int index = 0;

		int accountCount = Integer.parseInt(parts[index++]);
		HashMap<String, Double> accountDetails = new HashMap<String, Double>(accountCount);
		for (int i = 0; i < accountCount; i++) {
			String account = parts[index++];
			Double balance = Double.parseDouble(parts[index++]);
			accountDetails.put(account, balance);
		}
		
		int logCount = Integer.parseInt(parts[index++]);
		HashMap<String, Trxn> trxnLog = new HashMap<String, Trxn>(logCount);
		for (int i = 0; i < logCount; i++) {
			String account = parts[index++];
			Trxn trxn = Trxn.parseString(parts[index++]);
			trxnLog.put(account, trxn);
		}
		
		return new Sync(accountDetails, trxnLog);
	}

	public HashMap<String, Double> getAccountDetails() {
		return accountDetails_;
	}

	public HashMap<String, Trxn> getTransactionLogs() {
		return trxnLog_;
	}
}
