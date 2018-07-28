package bm.main.engines;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import javax.net.ssl.*;

import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.HTTPEngine.HTTPEngineRequest;
import bm.main.engines.requests.HTTPEngine.HTTPEngineRequestType;

public class HTTPEngine extends AbstEngine {

	public HTTPEngine(String name, String logDomain, String errorLogDomain) {
		super(logDomain, errorLogDomain, name, HTTPEngine.class.toString());
	}

	@Override
	protected Object processRequest(EngineRequest er) {
		HTTPEngineRequest her = (HTTPEngineRequest) er;
		try {
			HTTPResponse response = sendHTTPRequest(her.getURL(), her.getRequestType(), her.getHeaders(), 
					her.getParameters(), her.doOutput());
			int i = response.getResponseCode();
			if(her.checkResponseCodeValidity(i)) {
				return response;
			} else {
				return new EngineException(this, "Server responded with error " + i);
			}
		} catch (IOException e) {
			return new EngineException(this, e);
		}
	}
	
	protected HTTPResponse sendHTTPRequest(String url, HTTPEngineRequestType method, HashMap<String, String> headers, 
			HashMap<String, String> parameters, boolean doOutput) 
			throws IOException {
		URLConnection conn = null;
		String params = "";
		try {
			conn = establishConnection(url, doOutput);
			if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
				((HttpURLConnection) conn).setRequestMethod(method.toString());
			//LATER HTTPEngine: The arguments below are crude. Fix if fixable.
			} else if(conn.getClass().getSimpleName().equals(HttpsURLConnection.class.getSimpleName() + "Impl")) {
				((HttpsURLConnection) conn).setRequestMethod(method.toString());
			} else {
				LOG.fatal(conn.getClass().getSimpleName());
				LOG.fatal(HttpsURLConnection.class.getSimpleName());
				throw new IOException("Invalid protocol!");
			}
		} catch (MalformedURLException e) {
			LOG.error("Malformed URL!");
			throw e;
		} catch (IOException e) {
			LOG.error("Cannot open connection to specified URL!");
			throw e;
		}
		
		LOG.trace("Conducting a " + method.toString() + " request to " + conn.getURL());
		
		//building headers part
		if(headers != null) {
			for(int i = 0; i < headers.size(); i++) {
				String key = headers.keySet().toArray()[i].toString();
				String value = headers.get(key);
				LOG.trace("Setting header '" + key + "' to '" + value + "'");
				conn.setRequestProperty(key, value);
			}
		}
		
		//building parameters part
		if(doOutput) {
			if(parameters != null) {
				for(int i = 0; parameters != null && i < parameters.size(); i++) {
					String key = parameters.keySet().toArray()[i].toString();
					String value = parameters.get(key);
					if(key.equals("null") || key == null) {
						params += value;
					} else {
						params += key + "=" + value;
					}
				}
				LOG.trace("Sending parameters '" + params + "'");
				DataOutputStream connOut = new DataOutputStream(conn.getOutputStream());
				connOut.writeBytes(params);
				connOut.flush();
				connOut.close();
			}
		}
		
		//Get Response  
		HTTPResponse res = null;
		try {
		    if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
		    		res = new HTTPResponse(((HttpURLConnection) conn).getResponseCode(), conn.getInputStream());
			} else {
				res = new HTTPResponse(((HttpsURLConnection) conn).getResponseCode(), conn.getInputStream());
			} 
		} catch(FileNotFoundException e) { //if there is no response
			if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
		    		res = new HTTPResponse(((HttpURLConnection) conn).getResponseCode());
			} else {
				res = new HTTPResponse(((HttpsURLConnection) conn).getResponseCode());
			} 
		}
		return res;
	}
	
	/**
	 * Establishes an HTTP connection with the specified URL
	 * 
	 * @param url The URL where the connection will take place
	 * @return An <i>HttpURLConnection</i>
	 * @throws IOException if connection is not possible
	 * @throws MalformedURLException if URL is malformed
	 */
	private URLConnection establishConnection(String url, boolean doOutput) throws IOException, MalformedURLException {
		URL urlObj = new URL(url);
		URLConnection conn;
		LOG.trace("Establishing connection at " + url);
		conn = urlObj.openConnection();
		conn.setDoOutput(doOutput);
		return conn;
	}
	
	public class HTTPResponse {
		private int responseCode;
		private InputStream inputStream;
		
		protected HTTPResponse(int responseCode, InputStream inputStream) {
			this.responseCode = responseCode;
			this.inputStream = inputStream;
		}
		
		protected HTTPResponse(int responseCode) {
			this.responseCode = responseCode;
			this.inputStream = null;
		}
		
		/**
		 * Returns the response code of the requested server
		 * 
		 * @return the response code
		 */
		public int getResponseCode() {
			return responseCode;
		}
		
		/**
		 * Returns the response of the requested server in String format. <b><i>WARNING:</b> Once this method is invoked, 
		 * the input stream specified with this HTTPResponse will no longer be available</i>
		 * 
		 * @return the response, <i>null</i> if there is no response
		 */
		public String getResponse() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		    StringBuilder response = new StringBuilder();
		    String line;
		    while ((line = reader.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    reader.close();
			return response.toString();
		}
		
		/**
		 * Returns the input stream for the HTTP response of the requested server
		 * 
		 * @return the input stream, <i>null</i> if there is no response
		 */
		public InputStream getInputStream() {
			return inputStream;
		}
	}
}
