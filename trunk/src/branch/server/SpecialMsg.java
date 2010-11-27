package branch.server;

public class SpecialMsg {

	private static String msgSeparator = ";;";
	
	enum Type {
		CHAIN_ACK, SYNC, VIEW
	}

	Type type_;
	View view_;
	String ackSerialNum_;
	Sync sync_;
	
	public SpecialMsg(Type type, View view, String serNum, Sync sync) {
		this.type_ = type;
		this.view_ = view;
		this.ackSerialNum_ = serNum;
		this.sync_ = sync;
	}
	
	public SpecialMsg(View view) {
		view_ = view;
		type_ = Type.VIEW;
		sync_ = null;
		ackSerialNum_ = null;
	}
	
	public SpecialMsg(Sync sync) {
		sync_ = sync;
		type_ = Type.SYNC;
		view_ = null;
		ackSerialNum_ = null;
	}
	
	public SpecialMsg(String serialNum) {
		type_ = Type.CHAIN_ACK;
		ackSerialNum_ = serialNum;
		sync_ = null;
		view_ = null;
	}
		
	public SpecialMsg.Type getType() {
		return type_;
	}
	
	public View getView() {
		return view_;
	}
	
	public Sync getSync() {
		return sync_;
	}
	
	public String getAckSerialNum() {
		return ackSerialNum_;
	}

	public String toString() {
		String str = type_.toString() + msgSeparator;
		switch (type_) {
		case CHAIN_ACK:
			str += ackSerialNum_;
			break;
		case SYNC:
			str += sync_.toString();
			break;
		case VIEW:
			str += view_.toString();
		}

		return str;
	}

	public static SpecialMsg parseString(String str) {
		String[] parts = str.split(msgSeparator);
		Type type = Type.valueOf(parts[0]);
		String ackSerNum = null;
		Sync sync = null;
		View view = null;

		switch (type) {
		case CHAIN_ACK:
			ackSerNum  = parts[1];
			break;
		case SYNC:
			sync = Sync.parseString(parts[1]);
			break;
		case VIEW:
			view = View.parseString(parts[1]);
			break;
		}
		
		return new SpecialMsg(type, view, ackSerNum, sync);
	}
}
