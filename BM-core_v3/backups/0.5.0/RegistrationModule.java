package main.modules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import components.AbstComponent;
import components.CommonComponent;
import components.factories.AbstComponentFactory;
import components.products.AbstProduct;
import components.products.CommonProduct;
import components.properties.AbstProperty;
import components.properties.InnateProperty;
import components.properties.StringProperty;
import json.RRP.ReqRegister;
import json.RRP.ReqRequest;
import json.RRP.ResError;
import json.RRP.ResPOOP;
import json.RRP.ResRegister;
import main.BusinessMachine;
import main.ConfigLoader;
import main.engines.DBEngine;
import main.engines.exceptions.EngineException;
import main.engines.requests.DBEngine.InsertDBEReq;
import main.engines.requests.DBEngine.RawDBEReq;
import main.engines.requests.DBEngine.SelectDBEReq;
import main.engines.requests.DBEngine.UpdateDBEReq;
import main.repositories.ComponentRepository;
import mqtt.MQTTListener;
import tools.IDGenerator;

public class RegistrationModule extends AbstModule {
	private String comsTable = ""; //COMPONENTS table
	private String propsTable = ""; //PROPERTIES table
	private IDGenerator idg = new IDGenerator();
	private DBEngine dbe;
	private String productQuery;
	private String nameParam;
	private String prodIDParam;
	private String roomIDParam;
	private String poopRTY;
	private String stringPropTypeID;
	private String innatePropTypeID;
	private String ohIP;
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY, String poopRTY, 
			String nameParam, String prodIDParam, String roomIDParam, MQTTHandler mh, 
			ComponentRepository components, DBEngine dbe, String productQuery, 
			String stringPropTypeID, String innatePropTypeID, String ohIP) {
		super(logDomain, errorLogDomain, "RegistrationModule", RTY, new String[]{nameParam, roomIDParam}, 
				mh, components);
		this.dbe = dbe;
		this.productQuery = productQuery;
		this.nameParam = nameParam;
		this.prodIDParam = prodIDParam;
		this.roomIDParam = roomIDParam;
		this.poopRTY = poopRTY;
		this.innatePropTypeID = innatePropTypeID;
		this.stringPropTypeID = stringPropTypeID;
		this.ohIP = ohIP;
	}

	/**
	 * Registers component into system.
	 */
	@Override
	protected void process(ReqRequest request) {
		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, prodIDParam, roomIDParam);
		ApplicationContext appContext = BusinessMachine.getApplicationContext();
		//AbstComponentFactory cf;
		//AbstProductFactory pf;
		if(request.getJSON().has("exists")) {
			returnExistingComponent(request);
			return;
		}
		
		mainLOG.info("Registering component " + reg.mac + " to system...");
		Vector<String> ids = new Vector<String>(1,1);
		String ssid; //SSID of new component
		
		//getting Component product and properties
		/*try {
			//getting existing Component SSIDs
			mainLOG.debug("Retrieving existing SSIDs from DB...");
			SelectDBEReq dber1 = new SelectDBEReq(idg.generateMixedCharID(10), 
					"components", new String[]{"ssid"});
			Object o = forwardEngineRequest(dbe, dber1);
			if(!o.getClass().equals(ResError.class)) {
				ResultSet rs1 = (ResultSet) o;
				while(rs1.next()) {
					ids.add(rs1.getString("ssid"));
				}
				rs1.close();
				mainLOG.debug("Existing SSIDs retrieved!");
			}
			ssid = idg.generateMixedCharID(4, ids.toArray(new String[0])); 
			//getting Component product properties
			mainLOG.debug("Retrieving Component product properties...");
			RawDBEReq dber2 = new RawDBEReq(idg.generateMixedCharID(10), 
					productQuery + " and cpl.COM_TYPE = '" + reg.cid + "'");
			o = forwardEngineRequest(dbe, dber2);
			if(!o.getClass().equals(ResError.class)) {
				ResultSet rs2 = (ResultSet) o;
				String prod_ssid = null;
				while(rs2.next()) {
					prod_ssid = rs2.getString("prod_ssid");
				}
				try {
					product = (Product) appContext.getBean(prod_ssid);
				} catch(NoSuchBeanDefinitionException e) {
					product = (Product) appContext.getBean("CommonProductFactory");
				}
				product.initialize(prod_ssid, name, description, OH_icon, properties, rs);
				mainLOG.debug("Component product properties retrieved!");
			}
		} catch (SQLException e) {
			error(new ResError(reg, "Cannot process register request!"));
			e.printStackTrace();
			return;
		}*/
		
		//creation of Component object within BM system
		mainLOG.debug("Creating Component object...");
		String topic = ssid + "_topic";
		AbstComponent c = product.createComponent(ssid, reg.mac, reg.name, topic, reg.room, true);
		cr.addComponent(c);
		mainLOG.info("Component " + c.getSSID() + " created!");
		try {
			c.integrateComponentToSystem(logDomain);
			//c.persistComponentToDB(comsTable, propsTable, logDomain);
		} catch(HTTPException e) {
			mainLOG.error("Error " + e.getStatusCode() + " from OpenHAB!");
			error(e);
		} catch (Exception e) {
			mainLOG.error("Error in persisting component to DB! This component may not exist after "
					+ "the BM restarts!");
			error(e);
			e.printStackTrace();
		}
		
		//updates OH
		/*try {
			c.addOHItem(logDomain, ohIP);
		} catch(HTTPException e) {
			mainLOG.error("Error " + e.getStatusCode());
			error(e);
		}
		catch (Exception e) {
			mainLOG.error("Cannot add component " + c.getSSID() + " to OpenHAB!");
			error(e);
		}*/
		
		//publishing of Component credentials to default topic
		mainLOG.debug("Publishing Component credentials to default topic...");
		c.publishCredentials(mh, requestType, logDomain);
		mainLOG.info("Registration complete!");
	}
	
	private void returnExistingComponent(ReqRequest request) {
		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, prodIDParam, roomIDParam);
		AbstComponent c = cr.getComponent(reg.mac);
		request.cid = c.getSSID();
		mainLOG.info("Component already exists in system as " + c.getSSID() + "! "
				+ "Returning existing credentials and property states.");
		c.publishCredentials(mh, requestType, logDomain);
		c.publishPropertyStates(mh, poopRTY, logDomain);
		
		mainLOG.info("Activating component...");
		try {
			c.setActive(true, logDomain);
		} catch (EngineException e) {
			Exception e1 = new Exception("Cannot activate component!", e);
			error(e1);
		}
	}

	/**
	 * Checks for the following deficiencies in the request:
	 * <ul>
	 * 	<li>CID already exists</li>
	 * 	<li>Invalid product ID</li>
	 * 	<li>Invalid room ID</li>
	 * </ul>
	 */
	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		mainLOG.trace("Additional secondary request parameter checking...");
		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, prodIDParam, roomIDParam);
		boolean b = true;
		
		mainLOG.trace("Checking MAC validity...");
		if(cr.containsComponent(reg.mac)) {
			request.getJSON().put("exists", true);
			return true;
		}
		try {
			mainLOG.trace("Checking productID validity...");
			RawDBEReq dber3 = new RawDBEReq(idg.generateMixedCharID(10), 
					productQuery + " and cpl.COM_TYPE = '" + reg.cid + "'");
			Object obj = forwardEngineRequest(dbe, dber3);
			if(obj.getClass().equals(ResError.class)) {
				return false;
			}
			ResultSet rs2 = (ResultSet) obj;
			if (!rs2.isBeforeFirst() ) {    
			    error(new ResError(reg, "Product ID is invalid!"));
			    rs2.close();
			    return false;
			}
			rs2.close();
			
			//ResultSet rs3 = dbe.selectQuery("ssid", "rooms");
			mainLOG.trace("Checking roomID validity...");
			b = false;
			SelectDBEReq dber4 = new SelectDBEReq(idg.generateMixedCharID(10), 
					"rooms");
			Object o = forwardEngineRequest(dbe, dber4);
			if(o.getClass().equals(ResError.class)) {
				error((ResError) o);
			} else {
				ResultSet rs3 = (ResultSet) o;
				while(rs3.next()) {
					//LOG.debug(rs3.getString("ssid"));
					if(reg.room.equals(rs3.getString("ssid")) || 
							reg.room.equalsIgnoreCase(rs3.getString("name"))) {
						b = true;
						request.getJSON().put(roomIDParam, rs3.getString("ssid"));
						rs3.close();
						break;
					}
				}
				rs3.close();
			}
			if(!b) {
				mainLOG.error("Room ID is invalid!");
				return false;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

	public void setComsTable(String comsTable) {
		this.comsTable = comsTable;
	}
	
	public void setPropsTable(String propsTable) {
		this.propsTable = propsTable;
	}
}
