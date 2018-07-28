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
import bm.jeep.ReqPOOP;
import bm.jeep.ReqRequest;
import bm.jeep.ResError;
import bm.jeep.ResPOOP;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.tools.IDGenerator;

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
public class POOPModule extends AbstModule {
	private DBEngine dbe;
	private CIRRepository cirr;
	private String propIDParam;
	private String propValParam;
	private String propsTable = ""; //PROPERTIES table
	private String oh_topic;
	private IDGenerator idg = new IDGenerator();

	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam, 
			String propValParam, String oh_topic, MQTTPublisher mp, DeviceRepository cr, 
			DBEngine dbe, CIRRepository cirr) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, mp, cr);
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
	protected void process(ReqRequest request) {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		Device d = dr.getDevice(poop.cid);
		Vector<AbstProperty> propsToUpdate = new Vector<AbstProperty>(1, 1);
		try {
			cirr.update();
		} catch (EngineException e1) {
			error("CIRRepository cannot update! Old rules may apply!", e1);
		}
		
		mainLOG.info("Changing component " + request.cid + " property " 
				+ poop.propSSID + " to " + poop.propValue + "...");
		if(d.getProperty(poop.propSSID).getValue().equals(poop.propValue)) {
			mainLOG.info("Property is already set to " + poop.propValue + "!");
			mp.publish(new ResPOOP(poop, poop.propSSID, poop.propValue));
		}
		else {
			mainLOG.debug("Updating component property in system...");
			try {
				AbstProperty prop = d.getProperty(poop.propSSID);
				prop.setValue(poop.propValue, logDomain, false);
				prop.setValue(poop.propValue);
				prop.publishPropertyValueToMQTT(mp, poop.rid, poop.cid, poop.rty);
				propsToUpdate.add(prop);
			} catch (AdaptorException e) {
				error("Cannot change property " + poop.propSSID + " of component " + poop.cid, e);
				return;
			}
			
			mainLOG.info("Updating affected component properties in system...");
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
						mainLOG.info("Changing commponent " + dev.getSSID() + " property " + 
								exec.getPropSSID() + " to " + exec.getPropValue() + "...");
						try {
							prop2.setValue(exec.getPropValue(), logDomain, false);
							prop2.publishPropertyValueToMQTT(mp, poop.rid, dev.getSSID(), poop.rty);
							propsToUpdate.add(prop2);
							alreadyChanged.put(prop2, true);
							updatedOthers = true;
						} catch (AdaptorException e) {
							error("Cannot change property " + exec.getPropSSID() + " of component " + dev.getSSID(), 
									e);
							return;
						}
					} 
				}
			}
			if(!updatedOthers) mainLOG.info("No other components updated!");
		}
		mainLOG.debug("POOP processing complete!");
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
	protected boolean additionalRequestChecking(ReqRequest request) {
		boolean b = false;
		ReqPOOP poop = new ReqPOOP(request.getJSON(), propIDParam, propValParam);
		
		Device d = dr.getDevice(poop.cid);
		//System.out.println(c.getProperties().length + "--" + c.getProperty(poop.propSSID));
		if(d.getProperty(poop.propSSID) != null) { //checks if property exists in the component;
			AbstProperty prop = d.getProperty(poop.propSSID);
			if(!prop.checkValueTypeValidity(poop.propValue)) {
				ResError e = new ResError(poop, "Invalid value! Check property constraints and "
						+ "valid data types!");
				error(e);
				return false;
			} else b = true;
		}
		else {
			error(new ResError(poop, "Property does not exist in the specified component!"));
			return false;
		}
		
		return b;
	}

	public void setPropsTable(String s) {
		this.propsTable = s;
	}
}
