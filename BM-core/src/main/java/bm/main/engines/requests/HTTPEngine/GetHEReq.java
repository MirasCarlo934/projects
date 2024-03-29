package bm.main.engines.requests.HTTPEngine;

import java.util.HashMap;

/**
 * The HTTPEngineRequest representation of an HTTP GET request that the HTTPEngine will send to the specified URL.
 * 
 * @author carlomiras
 *
 */
public class GetHEReq extends HTTPEngineRequest {
	
	/**
	 * Creates an HTTPEngine request that the HTTPEngine uses to create an HTTP GET request that will be sent to the 
	 * specified URL.
	 * 
	 * @param id the ID of this engine request
	 * @param url the URL where the GET request will be sent to
	 * @param headers the headers of this GET request
	 * @param parameters the parameters of this GET request
	 * @param validResponseCodes the response codes that are valid for this request. Responses with codes that are not 
	 * 		specified here will be considered as <i>bad</i> responses, and the HTTPEngine will throw an exception
	 */
	public GetHEReq(String id, String url, HashMap<String, String> headers, HashMap<String, String> parameters, 
			int[] validResponseCodes) {
		super(id, url, HTTPEngineRequestType.GET, true, headers, parameters, validResponseCodes);
	}
}
