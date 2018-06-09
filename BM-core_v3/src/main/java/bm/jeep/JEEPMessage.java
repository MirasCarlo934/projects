package bm.jeep;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.comms.Sender;

public class JEEPMessage {
	protected String rid;
	protected String cid;
	protected String rty;
//	protected String topic;
	protected Protocol protocol;
	protected JSONObject json = new JSONObject();

	public JEEPMessage(JSONObject json, Protocol protocol) {
		this.rid = json.getString("RID");
		this.rty = json.getString("RTY");
		this.cid = json.getString("CID");
		this.json = json;
		this.protocol = protocol;
	}
	
	public JEEPMessage(String rid, String cid, String rty, Protocol protocol) {
		this.rid = rid;
		this.cid = cid;
		this.rty = rty;
		this.json.put("RID", rid);
		this.json.put("CID", cid);
		this.json.put("RTY", rty);
		this.protocol = protocol;
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
	public Protocol getProtocol() {
		return protocol;
	}
	
	@Override
	public String toString() {
		return json.toString();
	}
}
