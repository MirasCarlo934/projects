package bm.jeep.device;

import bm.comms.Sender;
import bm.jeep.JEEPMessage;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;

public class ResPOOP extends JEEPResponse {
	private String propSSID;
	private Object propVal;

	public ResPOOP(String rid, String cid, String rty, Sender sender, String propSSID, Object propVal) {
		super(rid, cid, rty, sender, true);
		setPropSSID(propSSID);
		setPropVal(propVal);
	}
	
	public ResPOOP(JEEPMessage message, String propSSID, Object propVal) {
		super(message, true);
		setPropSSID(propSSID);
		setPropVal(propVal);
	}

	public String getPropSSID() {
		return propSSID;
	}

	public void setPropSSID(String propSSID) {
		this.propSSID = propSSID;
		super.addParameter("property", propSSID);
	}

	public Object getPropVal() {
		return propVal;
	}

	public void setPropVal(Object propVal) {
		this.propVal = propVal;
		super.addParameter("value", propVal);
	}
}
