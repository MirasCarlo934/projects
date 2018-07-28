package bm.main.repositories;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bm.cir.exceptions.CIRSSyntaxException;
import bm.cir.objects.ArgOperator;
import bm.cir.objects.Argument;
import bm.cir.objects.Conditional;
import bm.cir.objects.ExecutionBlock;
import bm.cir.objects.Relationship;
import bm.cir.objects.Rule;
import bm.jeep.ReqPOOP;
import bm.main.engines.AbstEngine;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.FileEngine.GetInputStreamFEReq;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.main.engines.requests.FileEngine.ReadAllLinesFEReq;
import bm.main.engines.requests.FileEngine.UpdateFEReq;
import bm.main.engines.requests.FileEngine.VersionizeFileFEReq;
import bm.main.interfaces.Initializable;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.tools.IDGenerator;

public class CIRRepository /*extends AbstRepository*/ implements Initializable {
	private Logger LOG;
	private Rule[] rules = new Rule[0];
	private DeviceRepository cr;
	private FileEngine cirFE;
	private IDGenerator idg;
	
//	private SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
//	private SAXParser saxParser;
	private CIRFileParser fileParser;
	
	public CIRRepository(String logDomain, String errorLogDomain, DeviceRepository componentRepository, 
			FileEngine fe, IDGenerator idg) {
//		super(logDomain, CIRRepository.class.getSimpleName());
		LOG = Logger.getLogger(logDomain + "." + CIRRepository.class.getSimpleName());
		cr = componentRepository;
		this.cirFE = fe;
		this.idg = idg;
		fileParser = new CIRFileParser();
//		try {
//			saxParser = saxParserFactory.newSAXParser();
//		} catch (ParserConfigurationException | SAXException e) {
//			LOG.fatal("SAXParser cannot be instantiated!", e);
//		}
		LOG.info("CIRRepository started!");
	}
	
	@Override
	public void initialize() throws Exception {
		update();
	}
	
	/**
	 * Updates this CIRRepository by retrieving the CIR from the CIR files. The retrieved CIR will replace
	 * the ones that are currently in this repository.
	 */
	public void update() throws EngineException {
		LOG.debug("Updating CIRRepository...");
		cirFE.forwardRequest(new UpdateFEReq(idg.generateERQSRequestID()), Thread.currentThread(), true);
		rules = fileParser.retrieveCIRFromFile();
	}
	
//	public void versionizeCIR() {
//		try {
//			
//		} catch(EngineException e) {
//			LOG.error("Cannot versionize CIR file!", e);
//		}
//	}
	
	/**
	 * Overwrites the entire rules.cir file. <b>NOTE:</b> Existing CIR file will be versioned before it is overwritten
	 * 
	 * @param rules The new rules in XML format
	 */
	public void overwriteRules(String rules) {
		String[] lines = rules.split("\n");
		try {
			cirFE.forwardRequest(new VersionizeFileFEReq(idg.generateERQSRequestID()), Thread.currentThread(), 
					true);
			cirFE.forwardRequest(new OverwriteFileFEReq(idg.generateERQSRequestID(), lines), 
					Thread.currentThread(), true);
			LOG.info("CIR updated!");
		} catch (EngineException e) {
			LOG.error("Cannot overwrite rules.cir file! Retaining old rules...", e);
		}
		try {
			update();
		} catch (EngineException e) {
			LOG.error("Cannot read new rules.cir file! Retaining old rules...", e);
		}
	}
	
	/**
	 * Returns CIR that specify the given component and its specified property in their arguments block.
	 * 
	 * @param d The component to be checked
	 * @param p The property to be checked
	 * @return An array containing all the rules that specify the given component and its specified property in their 
	 * 		arguments block
	 */
	public Rule[] getSpecificRules(Device d, AbstProperty p) {
		LOG.debug("Retrieving rules for component " + d.getSSID() + "...");
		Vector<Rule> specRules = new Vector<Rule>(1,1);
		
		for(int i = 0; i < rules.length; i++) {
			Rule rule = rules[i];
			if(rule.containsArgument(d, p)) {
				specRules.addElement(rule);
			}
		}
		
		LOG.debug(specRules.size() + " rule/s retrieved!");
		return specRules.toArray(new Rule[0]);
	}
	
	/**
	 * Returns all the CIR in this CIRRepository
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
				Conditional rule_condition = Conditional.parseConditional(raw_rule.getAttributeValue("condition"));
				
				LOG.trace("Parsing rule \"" + rule_name + "\"...");
				
				//for parsing arguments block
				LOG.trace("Parsing arguments block of rule \"" + rule_name + "\"...");
				for(int j = 0; j < raw_args_coms.size(); j++) { //for each component argument
					Element raw_args_com = raw_args_coms.get(j);
					String cid = raw_args_com.getAttributeValue("id");
					Device dev;
					if(cr.containsComponent(cid)) { //checks if cid exists
						dev = cr.getDevice(cid);
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
						} else if(!dev.getProperty(pid).checkValueTypeValidity(pval)) {
							AbstProperty prop = dev.getProperty(pid);
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property value \"" + pval + 
									"\" for property " + prop.getStandardID() + " in arguments block. Disregarding rule!");
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
					if(cr.containsComponent(cid)) { //checks if cid exists
						dev = cr.getDevice(cid);
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
						} else if(!dev.getProperty(pid).checkValueTypeValidity(pval)) {
							AbstProperty prop = dev.getProperty(pid);
							LOG.warn("Rule \"" + rule_name + "\" contains invalid property value \"" + pval + 
									"\" for property " + prop.getStandardID() + " in execution block. Disregarding rule!");
							continue rulesLoop;
						}
						execs.add(new ExecutionBlock(cid, pid, pval));
					}
				}

				LOG.debug("Adding rule \"" + rule_name + "\" to repository");
				rules.add(new Rule(i, rule_name, rule_condition, args.toArray(new Argument[0]), 
						execs.toArray(new ExecutionBlock[0])));
			}
			
			return rules.toArray(new Rule[0]);
		}
	}
}
