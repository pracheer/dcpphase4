package branch.server;

/**
 * 
 * @author pg298, nk395
 * 
 * A Transaction (Trxn) class contains all information about a transaction.
 * It provides a processTransaction method to process it.
 *
 */

public class Trxn {

	public static final String msgSeparator = "::";

	public static enum TransxType { 
		DEPOSIT, WITHDRAW, QUERY, TRANSFER
	}

	private String serialNum_;
	private TransxType type_;

	private String sourceAccount_ = null;

	private String destAccount_ = null;

	private Double amount_ = (double) 0;

	public Trxn(String type, String serialNum, String amt, String acNo,
			String srcAcNo, String destAcNo) {

		serialNum_ = serialNum;
		type_ = TransxType.valueOf(type);

		switch (type_) {
		case DEPOSIT:
		case WITHDRAW:
			amount_ = Double.parseDouble(amt);
			/* fall through */
		case QUERY:
			sourceAccount_ = acNo;
			break;
		case TRANSFER:
			amount_ = Double.parseDouble(amt);
			sourceAccount_ = srcAcNo;
			destAccount_ = destAcNo;
			break;

		}

		if (type_ == TransxType.TRANSFER) {
			destAccount_ = destAcNo;
		}	
	}

	public Trxn(String serialNo, TransxType type, String sourceAccount,
			String destAccount, Double amount) {
		super();
		this.serialNum_ = serialNo;
		this.type_ = type;
		this.sourceAccount_ = sourceAccount;

		if (this.type_ == Trxn.TransxType.TRANSFER) {
			this.destAccount_ = destAccount;
		}

		this.amount_ = amount;
	}

	protected Trxn(String str) {
		String[] strs = str.split(msgSeparator);
		type_ = TransxType.valueOf(strs[0]);
		serialNum_ = strs[1];
		sourceAccount_ = strs[2];
		amount_ = Double.parseDouble(strs[3]);
		if (type_ == TransxType.TRANSFER) {
			destAccount_ = strs[4];
		}
	}

	public String toString() {
		return type_
		+ msgSeparator
		+ serialNum_
		+ msgSeparator
		+ sourceAccount_
		+ msgSeparator
		+ amount_
		+ (type_ == TransxType.TRANSFER ? msgSeparator + destAccount_ : "");
	}

	public static Trxn parseString(String str) {
		return new Trxn(str);
	}

	public String getSerialNum() {
		return serialNum_;
	}

	public TransxType getType() {
		return type_;
	}

	public String getSourceBranch() {
		try {
			return sourceAccount_.substring(0, 2);
		} catch (NumberFormatException e) {
			System.err.println("Invalid source branch Id. Branch Id has to be an integer");
			System.err.println(e.getMessage());
		}
		return null;
	}

	public String getSourceAccount() {
		return sourceAccount_;
	}

	public String getDestBranch() {
		return destAccount_.substring(0, 2);
	}

	public String getDestAccount() {
		return destAccount_;
	}

	public Double getAmount() {
		return amount_;
	}

	public void setSerialNum(String serialNum) {
		serialNum_ = serialNum;
	}

	public String getPrettyString() {
		String str = "";

		str += "(" + type_.toString() + ") ";		
		str += "Serial: " + serialNum_;

		if (type_ == TransxType.TRANSFER) {
			str += " FromAccount: " + sourceAccount_;
			str += " ToAccount: " + destAccount_;
			str += " Amount: " + amount_;
		} else {
			str += " Account: " + sourceAccount_;
			str += " Amount: " + amount_;
		}

		return str;
	}


}
