package branch.server;
import java.util.HashMap;

/**
 * @author pg298@cornell.edu
 * 
 */

/*
 * AccDetails class:
 * Contains a HashMap 'accountDetails' from 'AccountNumber' to 'Amount'.
 * For each transaction request, the accountDetails is updated.
 */
public class AccDetails {
	private HashMap<String, Double> accountDetails;

	public AccDetails() {	
		accountDetails = 
			new HashMap<String, Double>(100);
	}

	public synchronized Double deposit(String acnt, Double depositAmt) {
		Double finalAmt;
		if (accountDetails.containsKey(acnt)) {
			finalAmt = accountDetails.get(acnt) + depositAmt;
		} else {
			finalAmt = depositAmt;
		}

		accountDetails.put(acnt, finalAmt);

		return finalAmt;
	}

	public synchronized Double withdraw(String acnt, Double withdrawAmt) {
		
		Double existingAmt = 0.0;
		if (accountDetails.containsKey(acnt)) 
			existingAmt = accountDetails.get(acnt);

		Double finalAmt = existingAmt - withdrawAmt;

		accountDetails.put(acnt, finalAmt);

		return finalAmt;
	}

	public synchronized Double query(String acnt) {
		if (!accountDetails.containsKey(acnt))
			accountDetails.put(acnt, 0.0);
		
		return accountDetails.get(acnt);
	}

	@SuppressWarnings("unchecked")
	public synchronized HashMap<String,Double> getAllAccnts() {
		return (HashMap<String, Double>)accountDetails.clone();
	}
	
	public synchronized void synchronizeAccounts(Sync sync) {
		accountDetails = sync.getAccountDetails();
	}
}
