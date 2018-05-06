package bm.main.modules.admin;

import org.json.JSONException;

import bm.jeep.JEEPRequest;
import bm.jeep.admin.JEEPAdminRequest;
import bm.main.modules.*;
import bm.main.repositories.DeviceRepository;
import bm.tools.BMCipher;

public abstract class AbstAdminModule extends SimpleModule {
	private BMCipher cipher;
	private String encryptedPwd;
	private String pwdParam;

	public AbstAdminModule(String logDomain, String errorLogDomain, String name, String RTY, String[] params,
			DeviceRepository dr, BMCipher cipher, String pwdParam, String encryptedPwd) {
		super(logDomain, errorLogDomain, name, RTY, params, dr);
		this.cipher = cipher;
		this.encryptedPwd = encryptedPwd;
		this.pwdParam = pwdParam;
	}

	public AbstAdminModule(String logDomain, String errorLogDomain, String name, String RTY, String[] params,
			DeviceRepository dr, BMCipher cipher, String pwdParam, String encryptedPwd, 
			AbstModuleExtension[] extensions) {
		super(logDomain, errorLogDomain, name, RTY, params, dr, extensions);
		this.cipher = cipher;
		this.encryptedPwd = encryptedPwd;
		this.pwdParam = pwdParam;
	}
	
//	@Override
//	public void setRequest(JEEPRequest request) {
//		try {
//			this.request = (JEEPAdminRequest) request;
//		} catch(ClassCastException e) {
//			mainLOG.error("Request is not a JEEPAdminRequest!", e);
//		}
//	}

	@Override
	protected boolean checkSecondaryRequestParameters(JEEPRequest request) {
//		JEEPAdminRequest adminReq = (JEEPAdminRequest) request;

		String reqPwd;
		try {
			reqPwd = request.getJSON().getString(pwdParam);
		} catch(JSONException e) {
			mainLOG.error("No password supplied!", e);
			return false;
		}
		
		String actualPwd = cipher.decrypt(encryptedPwd);
		if(reqPwd.equals(actualPwd)) {
			return super.checkSecondaryRequestParameters(request);
		} else {
			mainLOG.error("Invalid password given!");
			return false;
		}
	} 
}
