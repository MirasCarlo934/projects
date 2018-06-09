package bm.main.modules;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import bm.comms.mqtt.MQTTPublisher;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;
import bm.main.interfaces.Initializable;
import bm.main.repositories.DeviceRepository;
import bm.tools.SystemTimer;

public abstract class MultiModule extends SimpleModule implements Initializable {
	private String[] resParams;
	private SystemTimer sysTimer;
	private Thread current;
	private LinkedList<JEEPResponse> responseQueue = new LinkedList<JEEPResponse>();
	
	public MultiModule(String logDomain, String errorLogDomain, String name, String RTY, String[] reqParams, 
			String[] resParams, DeviceRepository dr, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, name, RTY, reqParams, /*mp, */dr);
		this.sysTimer = sysTimer;
		this.resParams = resParams;
	}
	
	public MultiModule(String logDomain, String errorLogDomain, String name, String RTY, String[] reqParams, 
			String[] resParams, DeviceRepository dr, AbstModuleExtension[] extensions, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, name, RTY, reqParams, /*mp, */dr, extensions);
		this.sysTimer = sysTimer;
		this.resParams = resParams;
	}
	
	@Override
	public void initialize() {
		while(!Thread.currentThread().isInterrupted()) {
			if(!responseQueue.isEmpty()) {
				JEEPResponse response = responseQueue.poll();
				mainLOG.debug("Processing response '" + response.getRID() + "'");
				processResponse(response);
			}
		}
	}
	
	public void returnResponse(JEEPResponse response) {
		mainLOG.debug("Response for request '" + response.getRID() + "' received!");
		responseQueue.add(response);
	}
	
	protected abstract void processResponse(JEEPResponse response);
	
	/**
	 * Checks if the response contains all the required secondary parameters
	 * 
	 * @param response The JEEPResponse object
	 * @return <b><i>True</b></i> if the request is valid, <b><i>false</i></b> if: <br>
	 * 		<ul>
	 * 			<li>There are missing secondary request parameters</li>
	 * 			<li>There are secondary request parameters that are null/empty</li>
	 * 			<li>The module-specific request parameter check failed
	 * 			<br><i>Each module can have additional request checks, see individual
	 * 			modules for more details.</i></li>
	 * 		</ul>
	 */
	protected boolean checkSecondaryResponseParameters(JEEPResponse response) {
		mainLOG.trace("Checking secondary request parameters...");
		boolean b = false; //true if request is valid
		
		if(resParams == null || resParams.length == 0) //there are no secondary request params
			b = true;
		else {
			for(int i = 0; i < getParams().length; i++) {
				String param = getParams()[i];
				if(request.getParameter(param) != null && !request.getParameter(param).equals("")) 
					b = true;
				else {
					error("Parameter '" + param + "' is either empty or nonexistent!",
							request.getProtocol());
					b = false;
					break;
				}
			}
			
			if(b) { //if basic parameter checking is good
				b = additionalResponseChecking(response);
			}
		}
		
		return b;
	}
	
	/**
	 * Used in case of additional response parameter checking. <i><b>Must always return true 
	 * if there are no additional response checking</b></i>
	 * 
	 * @param response The Request object
	 * @return <b>True</b> if Request checks out, <b>false</b> otherwise. 
	 */
	protected abstract boolean additionalResponseChecking(JEEPResponse response);
}
