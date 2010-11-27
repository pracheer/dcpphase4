package test;

import branch.server.Message;
import branch.server.Trxn;
import branch.server.TrxnResponse;
import junit.framework.TestCase;

public class MessageTest extends TestCase {
	Message msg_;
	Trxn transaction_;
	TrxnResponse tResponse_;
	
	
	public void testPrettyString() {
		String sourceNode = "G01";
		transaction_ = new Trxn(
				"T0100000001",
				Trxn.TransxType.TRANSFER,
				"01.12345",
				"02.12346",
				100.25);
		tResponse_ = new TrxnResponse(
				"T0100000001",
				TrxnResponse.Type.SNAPSHOT,
				100.25,	false, "");
		
		msg_ = new Message(sourceNode, Message.MsgType.REQ, transaction_, null);		
		
		assertEquals(
				"(TRANSFER) Serial: T0100000001 FromAccount: 01.12345 ToAccount: 02.12346 Amount: 100.25",
				msg_.getPrettyString());

		msg_ = new Message(sourceNode, Message.MsgType.RESP, null, tResponse_);
		
		assertEquals("", msg_.getPrettyString());
	}
}
