package bm.jeep;

import org.json.JSONObject;

import bm.comms.Sender;

public class JEEPMessage {
	protected String rid;
	protected String cid;
	protected String rty;
//	protected String topic;
	protected Sender sender;
	protected JSONObject json = new JSONObject();

	public JEEPMessage(JSONObject json, Sender sender) {
		this.rid = json.getString("RID");
		this.rty = json.getString("RTY");
		this.cid = json.getString("CID");
		this.json = json;
		this.sender = sender;
	}
	
	public JEEPMessage(String rid, String cid, String rty, Sender sender) {
		this.rid = rid;
		this.cid = cid;
		this.rty = rty;
		this.json.put("RID", rid);
		this.json.put("CID", cid);
		this.json.put("RTY", rty);
		this.sender = sender;
	}
	
	/**
	 * Returns the <i>Request ID</i> of this JEEPMessage
	 * 
	 * @return The RID
	 */
	public String getRID() {
		return rid;
	}
	
	/**
	 * Returns the <i>Component ID</i> of this JEEPMessage
	 * 
	 * @return The CID
	 */
	public String getCID() {
		return cid;
	}
	
	/**
	 * Returns the <i>Request Type</i> of this JEEPMessage
	 * 
	 * @return The RTY
	 */
	public String getRTY() {
		return rty;
	}
	
	public JSONObject getJSON() {
		return json;
	}
	
	/**
	 * Returns the Sender object for this JEEPMessage
	 * 
	 * @return The Sender object
	 */
	public Sender getSender() {
		return sender;
	}
	
	@Override
	public String toString() {
		return json.toString();
	}
}
