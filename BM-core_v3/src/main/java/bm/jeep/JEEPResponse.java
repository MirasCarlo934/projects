package bm.jeep;

import org.json.JSONObject;

import bm.comms.Sender;

public class JEEPResponse extends JEEPMessage {
	private boolean success;

	/**
	 * The constructor in case papa requests for it
	 * @param rid
	 * @param cid
	 * @param rty
	 
	public AbstResponse(String rid, String cid, String rty, boolean success) {
		this.rid = rid;
		this.cid = cid;
		this.rty = rty;
		this.success = success;
		json.put("RID", rid);
		json.put("RTY", rty);
		json.put("success", success);
	}*/
	
	/**
	 * Not the default, most intuitive, and most logical constructor
	 * @param rid
	 * @param cid
	 */
	public JEEPResponse(String rid, String did, String rty, Sender sender, boolean success) {
		super(rid, did, rty, sender);
		json.put("RID", rid);
		json.put("CID", did);
		json.put("RTY", rty);
		json.put("success", success);
		this.success = success;
	}
	
	/**
	 * Constructor for inbound JEEPResponse.
	 * 
	 * @param json
	 * @param sender
	 */
	public JEEPResponse(JSONObject json, Sender sender) {
		super(json, sender);
		this.success = json.getBoolean("success");
	}
	
	public JEEPResponse(JSONObject json, Sender sender, boolean success) {
		super(json, sender);
		json.put("success", success);
		this.success = success;
	}
	
	/**
	 * The default, most intuitive, and most logical constructor
	 * @param rid
	 * @param cid
	 */
	public JEEPResponse(JEEPMessage message, boolean success) {
		super(message.getJSON(), message.getSender());
		this.success = success;
	}
	
	protected void addParameter(String name, Object value) {
		json.put(name, value);
	}
	
	/**
	 * Returns whether or not the request was successfully processed
	 * @return <b><i>True</i></b> if successful, <b><i>false</i></b> otherwise
	 */
	public boolean isSuccessful() {
		return success;
	}
}
