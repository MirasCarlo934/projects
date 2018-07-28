package bm.jeep;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * This is the object populated when a register JSON transaction arrives from mqtt
 * Sample data is:
 * 		{"RID":"18fe34cf4fc1","CID":"ESP","RTY":"register","name":"Esp12e_RGB","roomID":"MasterBedroom","prodID":"0002"}
 */
public class ReqRegister extends AbstRequest{
	//we are setting the parameters as public to make it easier to access
	public String name;
	public String room;
	public String mac;
	public JSONObject properties;

	public ReqRegister(JSONObject json, String nameParam, String roomIDParam, String propsParam) {
		super(json);
		this.name = json.getString(nameParam);
		this.room = json.getString(roomIDParam);
		try {
			this.properties = json.getJSONObject(propsParam);
		} catch(JSONException e) {
			this.properties = null;
		}
		this.mac = json.getString("RID");
	}
}
