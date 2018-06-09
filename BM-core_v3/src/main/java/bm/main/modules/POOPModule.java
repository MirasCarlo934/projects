package bm.main.modules;

import java.util.Vector;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;
import bm.jeep.device.JEEPErrorResponse;
import bm.jeep.device.ReqPOOP;
import bm.jeep.device.ResPOOP;
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
//	private CIRManager cirr;
	private String propIDParam;
	private String propValParam;
	private IDGenerator idg = new IDGenerator();
	private Vector<String> affectedIDs = new Vector<String>(1, 1); //SSID of all properties affected by this POOP

	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam,
					  String propValParam, DeviceRepository dr, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				new String[]{propIDParam, propValParam}, /*mp, */dr, sysTimer);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
	}
	
	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam,
					  String propValParam, DeviceRepository dr, AbstModuleExtension[] extensions,
					  SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				new String[]{propIDParam, propValParam}, /*mp, */dr, extensions, sysTimer);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
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
//		Vector<Property> propsToUpdate = new Vector<Property>(1, 1);
//		try {
//			cirr.updateRules();
//		} catch (EngineException e1) {
//			error("CIRManager cannot updateRules! Old rules may apply!", e1, request.getProtocol());
//		}
		
		mainLOG.info("Changing property " + poop.propSSID + " of device " + d.getSSID() + " to "
                + poop.propValue + "...");
		if(d.getProperty(poop.propSSID).getValue().toString().equals(poop.propValue.toString())) {
			mainLOG.info("Property is already set to " + poop.propValue + "!");
			request.getProtocol().getSender().send(new ResPOOP(poop, poop.propSSID, poop.propValue));
		}
		else {
			mainLOG.debug("Updating property in system...");
			try {
				Property prop = d.getProperty(poop.propSSID);
//				prop.setValue(poop.propValue, logDomain, false);
                prop.setValue(poop.propValue);
				prop.update(logDomain, false);
//                ReqPOOP reqPoop = new ReqPOOP(idg.generateRID(), prop.getDevice().getSSID(), poopRTY,
//                        poop.getProtocol(), propIDParam, propValParam, prop.getSSID(), prop.getValue());
//                poop.getProtocol().send(reqPoop);
//				propsToUpdate.add(prop);
			} catch (AdaptorException e) {
				error("Cannot change property " + poop.propSSID + " of component " + poop.getCID(), e, 
						request.getProtocol());
				return false;
			}
			
//			mainLOG.info("Updating affected devices in environment...");
//			boolean updatedOthers = false;
//			Rule[] rules = cirr.getSpecificRules(d.getProperty(poop.propSSID));
//			HashMap<Property, Boolean> alreadyChanged = new HashMap<Property, Boolean>(1);
//
//			for(int k = 0; k < rules.length; k++) {
//				Rule rule = rules[k];
//				if(rule.isSatisfiedWith(dr.getAllDevices())) {
//					for(int l = 0; l < rule.getExecBlocks().length; l++) {
//						ExecutionBlock exec = rule.getExecBlocks()[l];
//						Device dev = dr.getDevice(exec.getDeviceID());
//						Property prop2 = dev.getProperty(exec.getPropertyID());
//						if(alreadyChanged.containsKey(prop2)) {
//							continue;
//						}
//						mainLOG.info("Changing device " + dev.getSSID() + " property " +
//								exec.getPropertyID() + " to " + exec.getPropertyValue() + "...");
//						try {
//							affectedIDs.add(dev.getSSID() + "-" + prop2.getSSID());
//							prop2.setValue(exec.getPropertyValue(), poop.getCID(), logDomain, false);
//                            ReqPOOP reqPoop = new ReqPOOP(idg.generateRID(), prop2.getDevice().getSSID(), poopRTY,
//                                    poop.getProtocol(), propIDParam, propValParam, prop2.getSSID(), prop2.getValue());
//                            poop.getProtocol().send(reqPoop, null);
//							propsToUpdate.add(prop2);
//							alreadyChanged.put(prop2, true);
//							updatedOthers = true;
//						} catch (AdaptorException e) {
//							error("Cannot change property " + exec.getPropertyID() +
//									" of component " + dev.getSSID(),
//									e, request.getProtocol());
//							return false;
//						}
//					}
//				}
//			}
//			if(!updatedOthers) mainLOG.info("No other components updated!");
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
			Property prop = d.getProperty(poop.propSSID);
			if(!prop.checkValueValidity(poop.propValue)) {
				JEEPErrorResponse errorRes = new JEEPErrorResponse(poop, "Invalid value! "
						+ "Check property constraints and valid data types!");
				error(errorRes);
				return false;
			} else b = true;
		}
		else {
			error(new JEEPErrorResponse(request, "Property does not exist in the specified device!"));
			return false;
		}
		
		return b;
	}
}
