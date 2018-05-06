package bm.jeep;

import org.json.JSONException;
import org.json.JSONObject;

import bm.comms.Sender;

public abstract class JEEPRequest extends JEEPMessage{
	
	public JEEPRequest(String rid, String cid, String rty, Sender sender) {
		super(rid, cid, rty, sender);
	}

	public JEEPRequest(JSONObject json, Sender sender) {
		super(json, sender);
	}
	
	public JEEPRequest(JEEPRequest request) {
		super(request.json, request.sender);
	}
	
	/**
	 * Returns the value attached to the parameter name specified.
	 * 
	 * @param paramName The parameter name
	 * @return The value attached to the parameter, <b><i>null</i></b> if <b>paramName</b> does not exist
	 */
	public Object getParameter(String paramName) {
		Object o;
		try {
			o = json.get(paramName);
		} catch(JSONException e) {
			o = null;
		}
		return o;
	}
	
	/**
	 * Returns the string value attached to the parameter name specified.
	 * 
	 * @param paramName The parameter name
	 * @return The value attached to the parameter, <b><i>null</i></b> if <b>paramName</b> does not exist or if it is not a string
	 */
	public String getString(String paramName) {
		return (String) getParameter(paramName);
	}
	
	/**
	 * Returns all the parameters of this <i>JEEPRequest</i>.
	 * 
	 * @return A string array containing the parameters
	 */
	public String[] getParameters() {
		String[] params = json.keySet().toArray(new String[0]);
		return params;
	}
	
	public JSONObject getJSON() {
		return json;
	}
	
	@Override
	public String toString() {
		return json.toString();
	}
}
