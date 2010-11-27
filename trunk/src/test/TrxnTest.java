package test;

import branch.server.Trxn;
import junit.framework.TestCase;


public class TrxnTest extends TestCase {
	Trxn transaction_;

	public void testPrettyString() {
		// Transfer
		transaction_ = new Trxn(
				"T0100000001",
				Trxn.TransxType.TRANSFER,
				"01.12345",
				"02.12346",
				100.25);
		
		assertEquals(
				"(TRANSFER) Serial: T0100000001 FromAccount: 01.12345 ToAccount: 02.12346 Amount: 100.25",
				transaction_.getPrettyString());
		
		transaction_ = new Trxn(
				"D0100000001",
				Trxn.TransxType.DEPOSIT,
				"01.12345",
				null,
				1000.25);
		
		assertEquals(
				"(DEPOSIT) Serial: D0100000001 Account: 01.12345 Amount: 1000.25",
				transaction_.getPrettyString());
	}

}
