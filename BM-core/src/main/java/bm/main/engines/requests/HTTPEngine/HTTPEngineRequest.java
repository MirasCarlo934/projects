package bm.main.engines.requests.HTTPEngine;

import java.util.HashMap;

import bm.main.engines.HTTPEngine;
import bm.main.engines.requests.EngineRequest;

public abstract class HTTPEngineRequest extends EngineRequest {
	protected String url;
	protected boolean doOutput;
	protected HTTPEngineRequestType type;
	protected HashMap<String, String> headers = null;
	protected HashMap<String, String> parameters = null;
	protected int[] validResponseCodes;

	public HTTPEngineRequest(String id, String url, HTTPEngineRequestType type, boolean doOutput,
			HashMap<String, String> headers, HashMap<String, String> parameters, int[] validResponseCodes) {
		super(id, HTTPEngine.class.toString());
		this.doOutput = doOutput;
		this.type = type;
		this.headers = headers;
		this.parameters = parameters;
		this.validResponseCodes = validResponseCodes;
		if(url.startsWith("http://") || url.startsWith("https://"))
			this.url = url;
		else
			this.url = "http://" + url;
	}
	
	public boolean checkResponseCodeValidity(int responseCode) {
		if(validResponseCodes ==  null || validResponseCodes.length == 0) {
			return true;
		}
		for(int i = 0; i < validResponseCodes.length; i++) {
			if(responseCode == validResponseCodes[i]) {
				return true;
			}
		}
		return false;
	}
	
	public int[] getValidResponseCodes() {
		return validResponseCodes;
	}
	
	public HTTPEngineRequestType getRequestType() {
		return type;
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	
	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public String getURL() {
		return url;
	}
	
	public boolean doOutput() {
		return doOutput;
	}
}
