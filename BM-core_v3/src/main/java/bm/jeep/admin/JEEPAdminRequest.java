package bm.jeep.admin;

import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPMessage;
import bm.jeep.JEEPRequest;

public class JEEPAdminRequest extends JEEPRequest {
	private String pwd;

	public JEEPAdminRequest(JSONObject json, Sender sender) {
		super(json, sender);
		this.pwd = json.getString("pwd");
	}
	
	/**
	 * Returns the pwd of this JEEPAdminRequest.
	 * <br/><br/>
	 * <b>NOTE:</b> The pwd is an encrypted string.
	 * 
	 * @return The encrypted pwd string
	 */
	public String getPwd() {
		return pwd;
	}

//	public JEEPAdminRequest(JEEPRequest request) {
//		super(request);
//	}
}
