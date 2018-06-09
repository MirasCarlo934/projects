package bm.cir;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.properties.Property;
import bm.jeep.device.ReqPOOP;
import bm.main.repositories.DeviceRepository;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import bm.cir.objects.ArgOperator;
import bm.cir.objects.Argument;
import bm.cir.objects.ExecutionBlock;
import bm.cir.objects.Relationship;
import bm.cir.objects.Rule;
import bm.context.devices.Device;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.main.engines.requests.FileEngine.UpdateFEReq;
import bm.main.engines.requests.FileEngine.VersionizeFileFEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

public class CIRManager implements Initializable, Runnable {
    private String logDomain;
	private Logger LOG;
	private Rule[] rules = new Rule[0];
	private MQTTPublisher mp;
	private DeviceRepository dr;
	private FileEngine cirFE;
	private IDGenerator idg;
	private CIRFileParser fileParser;
	private LinkedList<Property> propQueue = new LinkedList<Property>();

	private String poopRTY;
	private String poopPropIDParam;
	private String poopPropValParam;

	public CIRManager(String logDomain, DeviceRepository deviceRepository, FileEngine fe,
                      MQTTPublisher mqttPublisher, IDGenerator idg, String poopRTY,
                      String poopPropIDParam, String poopPropValParam) {
		LOG = Logger.getLogger(logDomain + "." + CIRManager.class.getSimpleName());
        this.dr = deviceRepository;
        this.mp = mqttPublisher;
		this.cirFE = fe;
		this.idg = idg;
		this.poopRTY = poopRTY;
		this.poopPropIDParam = poopPropIDParam;
		this.poopPropValParam = poopPropValParam;
		fileParser = new CIRFileParser();
		LOG.info("CIRManager started!");
	}
	
	@Override
	public void initialize() throws Exception {
		updateRules();
	}

	@Override
    public void run() {
	    while(!Thread.currentThread().isInterrupted()) {
            Property property = propQueue.poll();
            if(property != null) {
                LOG.debug("Property " + property.getCommonName() + " changed value. Checking CIR...");
                Rule[] rules = getRulesTriggered(property);
                if (rules.length == 0) {
                    LOG.debug("No rules found!");
                    continue;
                }
                LOG.info("Property " + property.getCommonName() + " value change triggered " + rules.length
                        + " rules! Executing...");
                for (Rule rule : rules) {
                    ExecutionBlock[] execs = rule.getExecBlocks();
                    for (ExecutionBlock exec : execs) {
                        Device dev = dr.getDevice(exec.getDeviceID());
                        Property prop = dev.getProperty(exec.getPropertyID());
                        LOG.debug("Updating property " + prop.getCommonName() + "...");
                        prop.setValue(exec.getPropertyValue());
                        try {
                            prop.update(logDomain, false);
                            LOG.info("Property " + prop.getCommonName() + " updated from rule \""
                                    + rule.getName() + "\"");
                        } catch (AdaptorException e) {
                            LOG.error("Could not updateRules " + prop.getCommonName() + "!");
                        }
                    }
                }
            }
        }
    }
	
	/**
	 * Updates this CIRManager by retrieving the CIR from the CIR files. The retrieved CIR will replace
	 * the ones that are currently in this repository.
	 */
	public void updateRules() throws EngineException {
		LOG.debug("Updating CIRManager...");
		cirFE.putRequest(new UpdateFEReq(idg.generateERQSRequestID(), cirFE), Thread.currentThread(), true);
		rules = fileParser.retrieveCIRFromFile();
	}

    /**
     * Specifies that the property changed value. Adds the property to the queue to be processed by
     * this CIRManager.
     * @param property The property that changed value
     */
	public void propertyChangedValue(Property property) {
        propQueue.add(property);
    }
	
	/**
	 * Overwrites the entire rules.cir file. <b>NOTE:</b> Existing CIR file will be versioned before it is overwritten
	 * 
	 * @param rules The new rules in XML format
	 */
	public void overwriteRules(String rules) {
		String[] lines = rules.split("\n");
		try {
			cirFE.putRequest(new VersionizeFileFEReq(idg.generateERQSRequestID(), cirFE), Thread.currentThread(), 
					true);
			cirFE.putRequest(new OverwriteFileFEReq(idg.generateERQSRequestID(), cirFE, lines), 
					Thread.currentThread(), true);
			LOG.info("CIR updated!");
		} catch (EngineException e) {
			LOG.error("Cannot overwrite rules.cir file! Retaining old rules...", e);
		}
		try {
			updateRules();
		} catch (EngineException e) {
			LOG.error("Cannot read new rules.cir file! Retaining old rules...", e);
		}
	}
	
	/**
	 * Returns CIR that specify the given component and its specified property in their arguments block.
	 * 
	 * @param p The property to be checked
	 * @return An array containing all the rules that specify the given component and its specified property in their 
	 * 		arguments block
	 */
	public Rule[] getSpecificRules(Property p) {
		LOG.trace("Retrieving rules for property " + p.getSystemName() + "...");
		Vector<Rule> specRules = new Vector<Rule>(1,1);
		
		for(int i = 0; i < rules.length; i++) {
			Rule rule = rules[i];
			if(rule.containsArgument(p)) {
				specRules.addElement(rule);
			}
		}
		
		LOG.trace(specRules.size() + " rule/s retrieved!");
		return specRules.toArray(new Rule[0]);
	}

    /**
     * Returns CIR triggered by the property value change.
     * @param p The property that changed value
     * @return The CIRs
     */
	public Rule[] getRulesTriggered(Property p) {
        LOG.trace("Retrieving rules triggered by property " + p.getCommonName() + " value change " +
                "to " + p.getValue());
        Vector<Rule> specRules = new Vector<Rule>(1,1);

        for(int i = 0; i < rules.length; i++) {
            Rule rule = rules[i];
            if(rule.containsArgument(p)) {
                boolean b = true;
                for(Argument arg : rule.getArguments()) {
                    Property argProp = dr.getDevice(arg.getDeviceID()).getProperty(arg.getPropertyID());
                    if(!argProp.getValue().toString().equals(arg.getPropertyValue())) {
                        b = false;
                        break;
                    }
                }
                if(b) {
                    specRules.add(rule);
                }
            }
        }

        LOG.trace(specRules.size() + " rule/s retrieved!");
        return specRules.toArray(new Rule[0]);
    }
	
	/**
	 * Returns all the CIR in this CIRManager
	 * 
	 * @return An array of Rule objects
	 */
	public Rule[] getAllRules() {
		return rules;
	}
	
	private class CIRFileParser {
		SAXBuilder saxBuilder = new SAXBuilder();
        Document cirFile;
		
		public CIRFileParser() {
			try {
				cirFile = saxBuilder.build(cirFE.getFile());
			} catch (JDOMException | IOException e) {
				LOG.error("Cannot parse CIR file!", e);
			}
		}
		
		/**
		 * Retrieves the CIR from the CIR file.
		 * @return An array of Rules objects
		 */
		@SuppressWarnings("unchecked")
		public Rule[] retrieveCIRFromFile() {
			try {
				cirFile = saxBuilder.build(cirFE.getFile());
			} catch (JDOMException | IOException e) {
				LOG.error("Cannot parse CIR file!", e);
			}
			Element root = cirFile.getRootElement();
			List<Element> raw_rules = root.getChildren("rule");
			Vector<Rule> rules = new Vector<Rule>(raw_rules.size());
			
			rulesLoop:
			for(int i = 0; i < raw_rules.size(); i++) {
				Element raw_rule = raw_rules.get(i);
				Element raw_args = raw_rule.getChild("arguments");
				Element raw_execs = raw_rule.getChild("execution");				
				List<Element> raw_args_coms = raw_args.getChildren("component");
				List<Element> raw_execs_coms = raw_execs.getChildren("component");
				Vector<Argument> args = new Vector<Argument>(1,1);
				Vector<ExecutionBlock> execs = new Vector<ExecutionBlock>(1,1);
				
				String rule_name = raw_rule.getAttributeValue("name");
//				Conditional rule_condition = Conditional.parseConditional(raw_rule.getAttributeValue("condition"));
				
				LOG.trace("Parsing rule \"" + rule_name + "\"...");
				
				//for parsing arguments block
				LOG.trace("Parsing arguments block of rule \"" + rule_name + "\"...");
				for(int j = 0; j < raw_args_coms.size(); j++) { //for each component argument
					Element raw_args_com = raw_args_coms.get(j);
					String cid = raw_args_com.getAttributeValue("id");
					Device dev;
					if(dr.containsDevice(cid)) { //checks if cid exists
						dev = dr.getDevice(cid);
					} else {
						LOG.warn("Rule \"" + rule_name + "\" contains invalid component \"" + cid + "\" in "
								+ "arguments block. Disregarding rule!");
						continue rulesLoop;
					}
					List<Element> raw_args_com_props = raw_args_com.getChildren("property");
					for(int k = 0; k < raw_args_com_props.size(); k++) { //for each component property argument
						Element raw_args_com_prop = raw_args_com_props.get(k);
						String pid = raw_args_com_prop.getAttributeValue("id");
						Object pval = raw_args_com_prop.getAttributeValue("value");
						if(!dev.containsProperty(pid)) {
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property \"" + pid + "\" in "
								+ " for component " + cid + " in arguments block. Disregarding rule!");
						} else if(!dev.getProperty(pid).checkValueValidity(pval)) {
							Property prop = dev.getProperty(pid);
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property value \"" + pval + 
									"\" for property " + prop.getOH_ID() + " in arguments block. Disregarding rule!");
						}
						ArgOperator operator = ArgOperator.translate(
								raw_args_com_prop.getAttributeValue("operator"));
						Relationship relationshipWithNextArgument;
						if(raw_args_com_prop.getAttributeValue("nextRelationship") != null) {
							relationshipWithNextArgument = Relationship.parseString(
									raw_args_com_prop.getAttributeValue("nextRelationship"));
						} else {
							relationshipWithNextArgument = Relationship.NONE;
						}
						args.add(new Argument(cid, pid, pval, operator, relationshipWithNextArgument));
					}
				}
				
				//for parsing execs block
				LOG.trace("Parsing execution block of rule \"" + rule_name + "\"...");
				for(int j = 0; j < raw_execs_coms.size(); j++) {
					Element raw_execs_com = raw_execs_coms.get(j);
					String cid = raw_execs_com.getAttributeValue("id");
					Device dev;
					if(dr.containsDevice(cid)) { //checks if cid exists
						dev = dr.getDevice(cid);
					} else {
						LOG.warn("Rule \"" + rule_name + "\" contains invalid component \"" + cid + "\" in "
								+ "execution block. Disregarding rule!");
						continue rulesLoop;
					}
					List<Element> raw_execs_com_props = raw_execs_com.getChildren("property");
					for(int k = 0; k < raw_execs_com_props.size(); k++) {
						Element raw_execs_com_prop = raw_execs_com_props.get(k);
						String pid = raw_execs_com_prop.getAttributeValue("id");
						Object pval = raw_execs_com_prop.getAttributeValue("value");
						if(!dev.containsProperty(pid)) {
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property \"" + pid + "\" in "
								+ " for component " + cid + " in execution block. Disregarding rule!");
							continue rulesLoop;
						} else if(!dev.getProperty(pid).checkValueValidity(pval)) {
							Property prop = dev.getProperty(pid);
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property value \"" + pval + 
									"\" for property " + prop.getOH_ID() + " in execution block. Disregarding rule!");
							continue rulesLoop;
						}
						execs.add(new ExecutionBlock(cid, pid, pval));
					}
				}

				LOG.debug("Adding rule \"" + rule_name + "\" to repository");
				rules.add(new Rule(i, rule_name/*, rule_condition*/, args.toArray(new Argument[0]), 
						execs.toArray(new ExecutionBlock[0])));
			}
			
			return rules.toArray(new Rule[0]);
		}
	}
}