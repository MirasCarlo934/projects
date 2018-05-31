package bm.main.modules;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import bm.cir.objects.ArgOperator;
import bm.cir.objects.Argument;
import bm.cir.objects.ExecutionBlock;
import bm.cir.objects.Rule;
import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.AbstProperty;
import bm.context.properties.Property;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;
import bm.jeep.device.JEEPErrorResponse;
import bm.jeep.device.ReqPOOP;
import bm.jeep.device.ReqRequest;
import bm.jeep.device.ResError;
import bm.jeep.device.ResPOOP;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.tools.IDGenerator;
import bm.tools.SystemTimer;

/**
 * The heart of the BM's functionality, the POOPModule executes the <b>P</b>roperty-<b>O</b>riented <b>O</b>rchestration 
 * <b>P</b>rocedure based on the JEEP request sent by components every time their properties change value. The POOP is 
 * executed by this module by:
 * 	<ol>
 * 		<li>Updating the property value of the requesting component</li>
 * 		<li>Checking the <b>C</b>omponent <b>I</b>nteracton <b>R</b>ules for other component properties that will be 
 * 			changed based on the requesting component's property change</li>
 * 		<li>Changing the property values of other components based on the CIR</li>
 * 	</ol>
 * 
 * @author carlomiras
 *
 */

//TASK Follow new JEEP request/response protocol
//TASK Handle JEEPResponses accordingly
public class POOPModule extends MultiModule {
	private DBEngine dbe;
	private CIRRepository cirr;
	private String propIDParam;
	private String propValParam;
	private String propsTable = ""; //PROPERTIES table
	private String oh_topic;
	private IDGenerator idg = new IDGenerator();
	private Vector<String> affectedIDs = new Vector<String>(1, 1); //SSID of all properties affected by this POOP

	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam, 
			String propValParam, String oh_topic, /*MQTTPublisher mp, */DeviceRepository dr, 
			DBEngine dbe, CIRRepository cirr, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				new String[]{propIDParam, propValParam}, /*mp, */dr, sysTimer);
		this.dbe = dbe;
		this.cirr = cirr;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.oh_topic = oh_topic;
	}
	
	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam, 
			String propValParam, String oh_topic, /*MQTTPublisher mp, */DeviceRepository dr, 
			DBEngine dbe, CIRRepository cirr, AbstModuleExtension[] extensions, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				new String[]{propIDParam, propValParam}, /*mp, */dr, extensions, sysTimer);
		this.dbe = dbe;
		this.cirr = cirr;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.oh_topic = oh_topic;
	}

	/**
	 * Updates the system of the property change of the requesting component and also the property
	 * changes of all the affected components according to CIR.
	 * 
	 * @param request The Request to be processed. <b>Must be</b> a <i>ReqPOOP</i> object.
	 */
	@Override
	protected boolean process(JEEPRequest request) {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		Device d = dr.getDevice(poop.getCID());
		Vector<AbstProperty> propsToUpdate = new Vector<AbstProperty>(1, 1);
		try {
			cirr.update();
		} catch (EngineException e1) {
			error("CIRRepository cannot update! Old rules may apply!", e1, request.getSender());
		}
		
		mainLOG.info("Changing component " + request.getCID() + " property " 
				+ poop.propSSID + " to " + poop.propValue + "...");
		if(d.getProperty(poop.propSSID).getValue().toString().equals(poop.propValue.toString())) {
			mainLOG.info("Property is already set to " + poop.propValue + "!");
			request.getSender().send(new ResPOOP(poop, poop.propSSID, poop.propValue), this);
		}
		else {
			mainLOG.debug("Updating device property in system...");
			try {
				AbstProperty prop = d.getProperty(poop.propSSID);
				prop.setValue(poop.propValue, logDomain, false);
				prop.setValue(poop.propValue);
				prop.sendPropertyValue(request.getSender()/*, poop.getRID(), poop.getCID(), poop.getRTY(), 
						propIDParam, propValParam*/);
				propsToUpdate.add(prop);
			} catch (AdaptorException e) {
				error("Cannot change property " + poop.propSSID + " of component " + poop.getCID(), e, 
						request.getSender());
				return false;
			}
			
			mainLOG.info("Updating affected devices in environment...");
			boolean updatedOthers = false;
			Rule[] rules = cirr.getSpecificRules(d, d.getProperty(poop.propSSID));
			HashMap<AbstProperty, Boolean> alreadyChanged = new HashMap<AbstProperty, Boolean>(1);
			
			for(int k = 0; k < rules.length; k++) {
				Rule rule = rules[k];
				if(rule.isSatisfiedWith(dr.getAllDevices())) {
					for(int l = 0; l < rule.getExecBlocks().length; l++) {
						ExecutionBlock exec = rule.getExecBlocks()[l];
						Device dev = dr.getDevice(exec.getComID());
						AbstProperty prop2 = dev.getProperty(exec.getPropSSID());
						if(alreadyChanged.containsKey(prop2)) {
							continue;
						}
						mainLOG.info("Changing device " + dev.getSSID() + " property " + 
								exec.getPropSSID() + " to " + exec.getPropValue() + "...");
						try {
							affectedIDs.add(dev.getSSID() + "-" + prop2.getSSID());
							prop2.setValue(exec.getPropValue(), poop.getCID(), logDomain, false);
							prop2.sendPropertyValue(request.getSender()/*, poop.getRID(), dev.getSSID(), 
									poop.getRTY(), propIDParam, propValParam*/);
							propsToUpdate.add(prop2);
							alreadyChanged.put(prop2, true);
							updatedOthers = true;
						} catch (AdaptorException e) {
							error("Cannot change property " + exec.getPropSSID() + 
									" of component " + dev.getSSID(), 
									e, request.getSender());
							return false;
						}
					} 
				}
			}
			if(!updatedOthers) mainLOG.info("No other components updated!");
		}
		mainLOG.info("POOP processing complete!");
		return true;
	}
	
	@Override
	protected void processResponse(JEEPResponse response) {
		ResPOOP res = new ResPOOP(response, propIDParam, propValParam);
		affectedIDs.remove(res.getCID() + res.getPropSSID());
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}

	/**
	 * Checks if request follows the following requirements:
	 * <ol>
	 * 	<li>Component with CID has the specified property</li>
	 * 	<li>Value specified is valid for the property</li>
	 * 	<ul>
	 * 		<li><b>For CommonProperty: <i>true</i></b> if value is an integer and is within min/max
	 * 			range of the specified property</li>
	 * 	</ul>
	 * </ol>
	 */
	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		boolean b = false;
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		
		Device d = dr.getDevice(poop.getCID());
		//System.out.println(c.getProperties().length + "--" + c.getProperty(poop.propSSID));
		if(d.getProperty(poop.propSSID) != null) { //checks if property exists in the component;
			AbstProperty prop = d.getProperty(poop.propSSID);
			if(!prop.checkValueTypeValidity(poop.propValue)) {
				JEEPErrorResponse errorRes = new JEEPErrorResponse(poop, "Invalid value! "
						+ "Check property constraints and valid data types!");
				error(errorRes);
				return false;
			} else b = true;
		}
		else {
			error(new JEEPErrorResponse(request, "Property does not exist in the specified component!"));
			return false;
		}
		
		return b;
	}

	public void setPropsTable(String s) {
		this.propsTable = s;
	}
}
