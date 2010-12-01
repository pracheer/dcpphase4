package branch.server;

/**
 * The response of a transaction request.
 * Can contain two types of responses.
 * 
 * TRANSACTION: Response for a general transaction like withdraw, deposit etc.
 * In this case it contains the serialNumber of the transaction, amount in the
 * corresponding account after the transaction and errorMsg if relevant.
 * SNAPSHOT: Response for a snapshot request.
 * In this case it contains a SnapshotResponse
 * which is basically a string representation of the snapshot.
 * 
 */

public class TrxnResponse {
	private String serialNum_;
	private String erroMsg_;
	private Double amt_;
	private boolean isError_;
	private Type type_;
	private String snapshotResponse_;
	
	public static enum Type {
		TRANSACTION
	}

	public TrxnResponse(String serialNum_, Type type, Double amt_, boolean status_,
			String erroMsg_) {
		this.serialNum_ = serialNum_;
		this.erroMsg_ = erroMsg_;
		this.amt_ = amt_;
		this.isError_ = status_;
		this.type_ = type;
	}
	
	public TrxnResponse(String serialNum, Type type, String snapshotResponse) {
		serialNum_ = serialNum;
		type_ = type;
		snapshotResponse_ = snapshotResponse;
	}

	public String toString() {
		if (type_ == Type.TRANSACTION) {
			return serialNum_ 
			+ Trxn.msgSeparator + type_
			+ Trxn.msgSeparator + amt_
			+ Trxn.msgSeparator + erroMsg_ 
			+ Trxn.msgSeparator	+ isError_;
		} else {
			return serialNum_ 
			+ Trxn.msgSeparator + type_
			+ Trxn.msgSeparator	+ snapshotResponse_;
		}
	}

	public static TrxnResponse parseString(String str) {		
		String[] strs = str.split(Trxn.msgSeparator);
		
		Type type = Type.valueOf(strs[1]);
		

		if (type == Type.TRANSACTION) {
			return new TrxnResponse(
					strs[0],
					type,
					Double.parseDouble(strs[2]),
					Boolean.valueOf(strs[4]),
					strs[3]);
		} else {
			System.err.println("Could not parse TransactionResponse: " + str);
			return null;
		} 
	}

	public String getSerialNum() {
		return serialNum_;
	}
	
	public Type getType() {
		return type_;
	}

	public String getErrorMsg() {
		return erroMsg_;
	}

	public Double getAmt() {
		return amt_;
	}
	
	public String getSnapshotResponse() {
		return snapshotResponse_;
	}
	
	public boolean getIsError() {
		return isError_;
	}

	public void setAmt(Double arg) {
		amt_ = arg;
	}

	public void setSerialNum(String newSerialNum) {
		serialNum_ = newSerialNum;
	}
}
