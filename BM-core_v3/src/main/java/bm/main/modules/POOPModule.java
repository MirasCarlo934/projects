package bm.main.modules;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.JEEPManager;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.ReqPOOP;
import bm.main.modules.exceptions.RequestProcessingException;
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
public class POOPModule extends Module {
//	private CIRManager cirr;
	private String propIDParam;
	private String propValParam;
	private IDGenerator idg = new IDGenerator();
	private JEEPManager jm;
//	private Vector<String> affectedIDs = new Vector<String>(1, 1); //SSID of all properties affected by this POOP

	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam,
					  String propValParam, DeviceRepository dr, JEEPManager jeepManager, SystemTimer sysTimer) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				null, /*mp, */dr);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.jm = jeepManager;
	}
	
//	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam,
//					  String propValParam, DeviceRepository dr, AbstModuleExtension[] extensions,
//					  SystemTimer sysTimer) {
//		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam},
//				null, /*mp, */dr);
//		this.propIDParam = propIDParam;
//		this.propValParam = propValParam;
//	}

	/**
	 * Updates the system of the property change of the requesting component and also the property
	 * changes of all the affected components according to CIR.
	 * 
	 * @param request The Request to be processed. <b>Must be</b> a <i>ReqPOOP</i> object.
	 */
	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		Device d = dr.getDevice(poop.getCID());
//		Vector<Property> propsToUpdate = new Vector<Property>(1, 1);
//		try {
//			cirr.updateRules();
//		} catch (EngineException e1) {
//			error("CIRManager cannot updateRules! Old rules may apply!", e1, request.getProtocol());
//		}
		
		LOG.info("Changing property " + poop.propIndex + " of device " + d.getSSID() + " to "
                + poop.propValue + "...");
		if(d.getProperty(poop.propIndex).getValue().toString().equals(poop.propValue.toString())) {
			LOG.info("Property is already set to " + poop.propValue + "!");
			jm.sendPOOPResponse(d.getProperty(poop.propIndex), poop);
		}
		else {
			LOG.debug("Updating property in system...");
			try {
				Property prop = d.getProperty(poop.propIndex);
//				prop.setValue(poop.propValue, logDomain, false);
                prop.setValue(poop.propValue);
				prop.update(logDomain, false);
				jm.sendPOOPResponse(prop, poop);
			} catch (AdaptorException e) {
//				error("Cannot change property " + poop.propIndex + " of device " + poop.getCID(), e,
//						request.getProtocol());
//				return false;
				throw new RequestProcessingException("Cannot change property " + poop.propIndex + " of device " +
						poop.getCID(), e);
			}
			
//			LOG.info("Updating affected devices in environment...");
//			boolean updatedOthers = false;
//			Rule[] rules = cirr.getSpecificRules(d.getProperty(poop.propIndex));
//			HashMap<Property, Boolean> alreadyChanged = new HashMap<Property, Boolean>(1);
//
//			for(int k = 0; k < rules.length; k++) {
//				Rule rule = rules[k];
//				if(rule.isSatisfiedWith(dr.getAllDevices())) {
//					for(int l = 0; l < rule.getExecBlocks().length; l++) {
//						ExecutionBlock exec = rule.getExecBlocks()[l];
//						Device dev = dr.getDevice(exec.getDeviceID());
//						Property prop2 = dev.getProperty(exec.getPropertyIndex());
//						if(alreadyChanged.containsKey(prop2)) {
//							continue;
//						}
//						LOG.info("Changing device " + dev.getSSID() + " property " +
//								exec.getPropertyIndex() + " to " + exec.getPropertyValue() + "...");
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
//							error("Cannot change property " + exec.getPropertyIndex() +
//									" of component " + dev.getSSID(),
//									e, request.getProtocol());
//							return false;
//						}
//					}
//				}
//			}
//			if(!updatedOthers) LOG.info("No other components updated!");
		}
		LOG.info("Property changed. POOP processing complete!");
//		return true;
	}
	
	@Override
	protected void processResponse(JEEPResponse response) {
//		ResPOOP res = new ResPOOP(response, propIDParam, propValParam);
//		affectedIDs.remove(res.getCID() + res.getPropSSID());
//		return true;
	}

	@Override
	public void processNonResponse(JEEPRequest request) {
		ReqPOOP poop = (ReqPOOP) request;
		Device device = dr.getDevice(request.getCID());
		LOG.warn("Device " + request.getCID() + " has not responded to a request to change its property "
				+ device.getProperty(poop.propIndex) + " (" + device.getProperty(poop.propIndex).getDisplayName()
				+ ") to " + poop.propValue);
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
	protected boolean additionalRequestChecking(JEEPRequest request) throws SecondaryMessageCheckingException {
		boolean b;
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		
		Device d = dr.getDevice(poop.getCID());
		//System.out.println(c.getPropvals().length + "--" + c.getProperty(poop.propIndex));
		if(d.getProperty(poop.propIndex) != null) { //checks if property exists in the component;
			Property prop = d.getProperty(poop.propIndex);
			if(!prop.checkValueValidity(poop.propValue)) {
//				JEEPErrorResponse errorRes = new JEEPErrorResponse(poop, "Invalid value! "
//						+ "Check property constraints and valid data types!");
//				error(errorRes);
				throw new SecondaryMessageCheckingException("Invalid value! Check valid data types for given " +
						"property!");
			} else b = true;
		}
		else {
//			error(new JEEPErrorResponse(request, "Property does not exist in the specified device!"));
			throw new SecondaryMessageCheckingException("Property does not exist in the specified device!");
		}
		
		return b;
	}
}
