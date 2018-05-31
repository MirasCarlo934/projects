package bm.main.modules;

import java.util.Vector;

import org.apache.log4j.Logger;

import bm.cir.objects.Rule;
import bm.comms.Sender;
import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.jeep.JEEPRequest;
import bm.jeep.device.JEEPErrorResponse;
import bm.jeep.device.ReqRequest;
import bm.jeep.device.ResError;
import bm.main.engines.AbstEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.repositories.DeviceRepository;

/**
 * The Module object handles the JEEP requests sent by Symphony components to the BM. A Module handles these requests by 
 * doing the following:
 * 	<ol>
 * 		<li>Checking of secondary request parameters</li>
 * 		<li>Management of devices, properties, and/or rooms based on the request</li>
 * 		<li>Sending of JEEP response back to the requesting component</li>
 * 	</ol>
 * 
 * @author carlomiras
 *
 */
public abstract class SimpleModule implements Runnable {
	protected String logDomain;
	protected Logger mainLOG;
	protected Logger errorLOG;
	protected AbstModuleExtension[] extensions = new AbstModuleExtension[0];
	protected String name;
	protected String requestType;
	private String[] params;
//	protected MQTTPublisher mp;
	protected DeviceRepository dr;
	protected JEEPRequest request;

	/**
	 * Creates a Module object with no extensions
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param name the name of this module
	 * @param RTY the JEEP request type that this module handles
	 * @param params the secondary request parameters for the JEEP requests this module will handle
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param dr the DeviceRepository of this BM
	 */
	public SimpleModule(String logDomain, String errorLogDomain, String name, String RTY, String[] 
			params, /*MQTTPublisher mp, */DeviceRepository dr) {
		mainLOG = Logger.getLogger(logDomain + "." + name);
		errorLOG = Logger.getLogger(errorLogDomain + "." + name);
		this.logDomain = logDomain;
		this.name = name;
		if(params == null) {
			params = new String[0];
		} else {
			this.params = params;			
		}
//		this.mp = mp;
		this.dr = dr;
		requestType = RTY;
	}
	
	/**
	 * Creates a Module object with extensions
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param name the name of this module
	 * @param RTY the JEEP request type that this module handles
	 * @param params the secondary request parameters for the JEEP requests this module will handle
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param dr the DeviceRepository of this BM
	 * @param extensions the ModuleExtensions attached to this Module
	 */
	public SimpleModule(String logDomain, String errorLogDomain, String name, String RTY, String[] 
			params, /*MQTTPublisher mp, */DeviceRepository dr, AbstModuleExtension[] extensions) {
		mainLOG = Logger.getLogger(logDomain + "." + name);
		errorLOG = Logger.getLogger(errorLogDomain + "." + name);
		this.logDomain = logDomain;
		this.name = name;
		this.params = params;
		this.dr = dr;
		this.extensions = extensions;
		requestType = RTY;
	}
	
	public void setRequest(JEEPRequest request) {
		this.request = request;
	}
	
	public void run() {
		mainLOG.debug(name + " request processing started!");
		
		if(checkSecondaryRequestParameters(request)) {
			mainLOG.trace("Request valid! Proceeding to request processing...");
			if(process(request)) {
				for(int i = 0; i < extensions.length; i++) {
					AbstModuleExtension ext = extensions[i];
					ext.processRequest(request);
				}
				mainLOG.info("Request processing finished!");
			} else {
				mainLOG.error(name + " did not process the request successfully!");
			}
		} else {
			mainLOG.error("Secondary request params didn't check out. See also the additional request params"
					+ " checking.");
		}
	}
	
	/**
	 * Forwards the supplied EngineRequest to the specified Engine. Handles the thread waiting
	 * procedure and error handling for the Engine response.
	 * 
	 * @param engine The Engine where the EngineRequest will be sent to
	 * @param engineRequest The EngineRequest that will be processed by the Engine
	 * @return The Engine response object. Returns ResError object if the Engine encountered
	 * 		an error during EngineRequest processing.
	 */
//	protected Object forwardEngineRequest(AbstEngine engine, EngineRequest engineRequest) {
//		engine.processRequest(engineRequest, Thread.currentThread());
//		try {
//			synchronized (Thread.currentThread()){Thread.currentThread().wait();}
//		} catch (InterruptedException e) {
//			mainLOG.error("Cannot stop thread!", e);
//			e.printStackTrace();
//		}
//		Object o = engine.getResponse(engineRequest.getId());
//		if(o.getClass().equals(EngineException.class)) {
//			EngineException error = (EngineException) o;
//			error(error);
//			return error;
//		}
//		else {
//			return o;
//		}
//	}
	
	/**
	 * Checks if the request contains all the required secondary parameters
	 * 
	 * @param request The Request object
	 * @return <b><i>True</b></i> if the request is valid, <b><i>false</i></b> if: <br>
	 * 		<ul>
	 * 			<li>There are missing secondary request parameters</li>
	 * 			<li>There are secondary request parameters that are null/empty</li>
	 * 			<li>The module-specific request parameter check failed
	 * 			<br><i>Each module can have additional request checks, see individual
	 * 			modules for more details.</i></li>
	 * 		</ul>
	 */
	protected boolean checkSecondaryRequestParameters(JEEPRequest request) {
		mainLOG.trace("Checking secondary request parameters...");
		boolean b = false; //true if request is valid
		
		if(params == null || params.length == 0) //there are no secondary request params
			b = true;
		else {
			for(int i = 0; i < getParams().length; i++) {
				String param = getParams()[i];
				if(request.getParameter(param) != null && !request.getParameter(param).equals("")) 
					b = true;
				else {
					error("Parameter '" + param + "' is either empty or nonexistent!", request.getSender());
					b = false;
					break;
				}
			}
			
			if(b) { //if basic parameter checking is good
				b = additionalRequestChecking(request);
			}
		}
		
		return b;
	}
	
	protected abstract boolean process(JEEPRequest request);
	
	/**
	 * Used in case of additional request parameter checking. <i><b>Must always return true 
	 * if there are no additional request checking</b></i>
	 * 
	 * @param request The Request object
	 * @return <b>True</b> if Request checks out, <b>false</b> otherwise. 
	 */
	protected abstract boolean additionalRequestChecking(JEEPRequest request);
	
	protected void error(String msg, Exception e, Sender sender) {
		mainLOG.error(msg);
		errorLOG.error(msg, e);
//		mp.publishToErrorTopic(msg + " (" + e.getMessage() + ")");
		sender.sendErrorResponse(new JEEPErrorResponse(msg + " (" + e.getMessage() + ")", sender));
	}
	
	protected void error(Exception e, Sender sender) {
		mainLOG.error(e.getMessage());
		errorLOG.error(e.getMessage(), e);
//		mp.publishToErrorTopic(e.getMessage());
		sender.sendErrorResponse(new JEEPErrorResponse(e.getMessage(), sender));
	}
	
	protected void error(String msg, Sender sender) {
		mainLOG.error(msg);
		errorLOG.error(msg);
//		mp.publishToErrorTopic(msg);
		sender.sendErrorResponse(new JEEPErrorResponse(msg, sender));
	}
	
	protected void error(ResError error, Exception e, Sender sender) {
		mainLOG.error(error.getMessage());
		errorLOG.error(error.getMessage(), e);
//		mp.publishToErrorTopic(error + " (" + e.getMessage() + ")");
		sender.sendErrorResponse(new JEEPErrorResponse(error + " (" + e.getMessage() + ")", sender));
	}
	
	protected void error(JEEPErrorResponse error) {
		mainLOG.error(error.getMessage());
		errorLOG.error(error.getMessage());
//		mp.publishToErrorTopic(error);
		error.getSender().sendErrorResponse(error);
	}

	/**
	 * @return the params
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(String[] params) {
		this.params = params;
	}
	
	/**
	 * Returns the name of this module.
	 * 
	 * @return The name
	 */
	public String getName() {
		return name;
	}
}
