package bm.jeep.device;

import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;

public class JEEPErrorResponse extends JEEPResponse {
	private boolean complete = true;
	private String message;

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
	public JEEPErrorResponse(String rid, String cid, String rty, Sender sender, String message) {
		super(rid, cid, rty, sender, false);
		this.message = message;
		addParameter("errormsg", message);
	}
	
	/**
	 * The default, most intuitive, and most logical constructor
	 * @param rid
	 * @param cid
	 */
	public JEEPErrorResponse(JEEPRequest request, String message) {
		super(request, false);
		this.message = message;
		addParameter("errormsg", message);
	}
	
	public JEEPErrorResponse(String message, Sender sender) {
		super(null, null, null, sender, false);
		this.message = message;
		addParameter("errormsg", message);
		complete = false;
	}
	
	/**
	 * Returns the error message contained by this DeviceJEEPErrorResponse.
	 * 
	 * @return The error message
	 */
	public String getMessage() {
		return message;
	}
	
	public boolean isComplete() {
		return complete;
	}
}
