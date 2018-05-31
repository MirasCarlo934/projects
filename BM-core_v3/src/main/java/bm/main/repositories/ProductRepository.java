package bm.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import bm.context.devices.products.*;
import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.context.properties.PropertyType;
import bm.context.properties.PropertyValueType;
import bm.context.properties.bindings.Binding;
import bm.main.Maestro;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

public class ProductRepository /*extends AbstRepository */ implements Initializable {
	private Logger LOG;
	private ProductFactory mainProdFactory; //the main product factory
	private String getProductsQuery;
	private String getPropertyTypesQuery;
	private IDGenerator idg;
	private DBEngine dbe;
	private HashMap<String, AbstProduct> products = new HashMap<String, AbstProduct>(5);
	private HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);
	private HashMap<String, ProductFactory> specialProducts;

	public ProductRepository(String logDomain, ProductFactory productFactory, DBEngine dbe, String getProductsQuery, 
			String getPropertyTypesQuery, IDGenerator idg, HashMap<String, ProductFactory> specialProducts) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductRepository.class.getSimpleName());
		this.mainProdFactory = productFactory;
		this.dbe = dbe;
		this.getProductsQuery = getProductsQuery;
		this.getPropertyTypesQuery = getPropertyTypesQuery;
		this.idg = idg;
		this.specialProducts = specialProducts;
	}

	@Override
	public void initialize() throws Exception {
		retrievePropertyTypes();
		mainProdFactory.setPropertyTypes(propertyTypes);
		Iterator<ProductFactory> specProds = specialProducts.values().iterator();
		while(specProds.hasNext()) {
			specProds.next().setPropertyTypes(propertyTypes);
		}
//		Iterator<ProductFactory> prodFactories = specialProducts.values().iterator();
//		while(prodFactories.hasNext()) {
//			ProductFactory pf = prodFactories.next();
//			pf.setPropertyTypes(propertyTypes);
//		}
		retrieveProducts();
	}
	
	public void retrieveProducts() {
		LOG.info("Populating products from DB...");
		
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getProductsQuery);
		
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		Object o2;
		try {
			o2 = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		
		ResultSet rs = (ResultSet) o; //for product retrieval
		ResultSet productRS = (ResultSet) o2; //for property retrieval in each product
		
//		try {
//			while(rs.next()) {
//				PropertyType prop_type = propertyTypes.get(rs.getString("prop_type"));
//				String prop_dispname = rs.getString("prop_dispname");
//				String prop_sysname = rs.getString("prop_sysname");
//				String prop_mode = rs.getString("prop_mode");
//				String pval_type = rs.getString("prop_val_type");
//				String prop_ssid = rs.getString("prop_index");
//				String prop_binding = rs.getString("prop_binding");
//				PropertyValueType pvt = PropertyValueType.parsePropValTypeFromString(pval_type);
//				Binding binding = Binding.parseBinding(prop_binding);
//				Property property = new Property(prop_type, prop_ssid, prop_sysname, prop_dispname, 
//						PropertyMode.parseModeFromString(prop_mode), 
//						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha, 
//						additionalAdaptors, binding);;
//			}
//		} catch(SQLException e) {
//			LOG.fatal("Cannot retrieve product properties from DB!", e);
//			return;
//		}
		
		try {
			String SSID = null;
			String name = null;
			String description = null;
			String OH_icon = null;
			while(rs.next()) {
				SSID = rs.getString("prod_ssid");
				name = rs.getString("prod_name");
				description = rs.getString("prod_desc");
				OH_icon = rs.getString("oh_icon");
				if(!products.containsKey(SSID)) {
					LOG.debug("Adding product " + SSID + " (" + name + ") to repository!");
					AbstProduct prod;
					if(specialProducts.containsKey(SSID)) {
						prod = specialProducts.get(SSID).createProductObject(SSID, name, description,
								OH_icon, productRS);
					} else {
						prod = mainProdFactory.createProductObject(SSID, name, description, OH_icon, productRS);
					}
					products.put(SSID, prod);
				}
			}
			rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve products from DB!", e);
		}
	}
	
//	public void retrieveProducts() {
//		LOG.info("Retrieving products from DB...");
//		HashMap<String, Property> prodProps = new HashMap<String, Property>(10); //product_id - property
//		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), getProductsQuery);
//		Object o;
//		try {
//			o = dbe.forwardRequest(request, Thread.currentThread(), true);
//		} catch (EngineException e1) {
//			LOG.fatal("Cannot retrieve products from DB!", e1);
//			return;
//		}
//		ResultSet rs = (ResultSet) o;
//		
//		try {
//			String SSID = null;
//			String name = null;
//			String description = null;
//			String OH_icon = null;
//			while(rs.next()) {
//				SSID = rs.getString("prod_ssid");
//				name = rs.getString("prod_name");
//				description = rs.getString("prod_desc");
//				OH_icon = rs.getString("oh_icon");
//				if(!products.containsKey(SSID)) {
//					LOG.debug("Adding product " + SSID + " (" + name + ") to repository!");
//					Product prod;
//					prod = pf.createProductObject(SSID, name, description, OH_icon);
//					products.put(SSID, prod);
//				}
//				
//				String prod_ssid =  rs.getString("prod_ssid");
//				PropertyType prop_type = propertyTypes.get(rs.getString("prop_type"));
//				String prop_dispname = rs.getString("prop_dispname");
//				String prop_sysname = rs.getString("prop_sysname");
//				String prop_mode = rs.getString("prop_mode");
////				String pval_type = rs.getString("prop_val_type");
//				String prop_ssid = rs.getString("prop_index");
//				String prop_binding = rs.getString("prop_binding");
////				PropertyValueType pvt = PropertyValueType.parsePropValTypeFromString(pval_type);
//				Binding binding = Binding.parseBinding(prop_binding);
//				Property prop = new Property(prop_type, prop_ssid, prop_sysname, prop_dispname, 
//						PropertyMode.parseModeFromString(prop_mode), dba, oha, additionalAdaptors, binding);
//			}
//			rs.close();
//		} catch (SQLException e) {
//			LOG.fatal("Cannot retrieve products from DB!", e);
//		}
//	}
	
	public void retrievePropertyTypes() {
		LOG.info("Retrieving property types from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getPropertyTypesQuery);
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		
		ResultSet rs = (ResultSet) o;
		try {
			while(rs.next()) {
				String ssid = rs.getString("ssid");
				String name = rs.getString("name");
				int min = rs.getInt("minim");
				int max = rs.getInt("maxim");
				String description = rs.getString("description");
				String oh_item = rs.getString("oh_item");
				String prop_transformable_type = rs.getString("prop_type");
				String pval_transformable = rs.getString("prop_value");
				String pval_command = rs.getString("oh_command");
				
				if(!propertyTypes.containsKey(ssid)) {
					LOG.debug("Adding property type " + ssid + " (" + name + ") to repository!");
					PropertyType propType = new PropertyType(ssid, name, description, oh_item, min, max);
//					propType.linkPropValueToOHCommand(pval_transformable, pval_command);
					propertyTypes.put(ssid, propType);
				}
				
				if(ssid.equals(prop_transformable_type) || propertyTypes.containsKey(prop_transformable_type)) {
					propertyTypes.get(prop_transformable_type).linkPropValueToOHCommand(pval_transformable, 
							pval_command);
				}
//				if(propertyTypes.containsKey(ssid) && propertyTypes.containsKey(prop_transformable_type)) {
//					propertyTypes.get(prop_transformable_type).linkPropValueToOHCommand(pval_transformable, pval_command);
//				} else {
//					if(!propertyTypes.containsKey(ssid) && ssid.equals(prop_transformable_type)) {
//						LOG.debug("Adding property type " + ssid + " (" + name + ") to repository!");
//						PropertyType propType = new PropertyType(ssid, name, description, oh_item, min, max);
//						propType.linkPropValueToOHCommand(pval_transformable, pval_command);
//						propertyTypes.put(ssid, propType);
//					} else {
//						LOG.debug("Adding property type " + prop_transformable_type + " (" + name + ") to repository!");
//						PropertyType propType = new PropertyType(prop_transformable_type, name, description, oh_item, min, max);
//						propType.linkPropValueToOHCommand(pval_transformable, pval_command);
//						propertyTypes.put(prop_transformable_type, propType);
//					}
//				}
			}
			rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve property types from DB!", e);
		}
	}

//	public void retrieveProducts() {
//		LOG.info("Retrieving products from DB...");
////		ApplicationContext appContext = BusinessMachine.getApplicationContext();
//		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), getProductsQuery);
//		Object o;
//		try {
//			o = dbe.forwardRequest(request, Thread.currentThread(), true);
//		} catch (EngineException e1) {
//			LOG.fatal("Cannot retrieve products from DB!", e1);
//			return;
//		}
//		Object o2;
//		try {
//			o2 = dbe.forwardRequest(request, Thread.currentThread(), true);
//		} catch (EngineException e1) {
//			LOG.fatal("Cannot retrieve products from DB!", e1);
//			return;
//		}
//		ResultSet rs = (ResultSet) o;
//		ResultSet productRS = (ResultSet) o2;
//		try {
//			String SSID = null;
//			String name = null;
//			String description = null;
//			String OH_icon = null;
//			while(rs.next()) {
//				SSID = rs.getString("prod_ssid");
//				name = rs.getString("prod_name");
//				description = rs.getString("prod_desc");
//				OH_icon = rs.getString("oh_icon");
//				if(!products.containsKey(SSID)) {
//					LOG.debug("Adding product " + SSID + " (" + name + ") to repository!");
//					Product prod;
//					prod = pf.createProductObject(SSID, name, description, OH_icon, productRS);
//					products.put(SSID, prod);
//				}
//			}
//			rs.close();
//		} catch (SQLException e) {
//			LOG.fatal("Cannot retrieve products from DB!", e);
//		}
//	}
	
//	public void addProduct(String SSID, String name, String description, String OH_icon, 
//			Property[] props) {
//		LOG.debug("Adding new product " + SSID + " (" + name + ")");
////		ApplicationContext appContext = BusinessMachine.getApplicationContext();
//		Product product = pf.createProductObject(SSID, name, description, OH_icon, props);
////		product.initialize(SSID, name, description, OH_icon, props);
//		products.put(SSID, product);
//	}
	
	public boolean containsProduct(String prodID) {
//		System.out.println("ProductRepository295:" + products.containsKey("0008"));
		return products.containsKey(prodID);
	}
	
	public AbstProduct[] getAllProducts() {
		return products.values().toArray(new AbstProduct[products.size()]);
	}
	
	public PropertyType[] getAllPropertyTypes() {
		return propertyTypes.values().toArray(new PropertyType[propertyTypes.size()]);
	}
	
	/**
	 * Returns the product associated with the specified prodID
	 * @param prodID The product SSID
	 * @return The product object, <b><i>null</i></b> if product with specified prodID does not exist.
	 */
	public AbstProduct getProduct(String prodID) {
		return products.get(prodID);
	}
	
	/**
	 * Returns the property type associated with the specified ptypeID
	 * @param ptypeID The property type SSID
	 * @return The property type object, <b><i>null</i></b> if property type with specified ptypeID does not exist.
	 */
	public PropertyType getPropertyType(String ptypeID) {
		return propertyTypes.get(ptypeID);
	}
	
	/**
	 * Adds the specified property type to this ProductRepository
	 * @param ptype The property type to be added
	 */
	public void addPropertyType(PropertyType ptype) {
		propertyTypes.put(ptype.getSSID(), ptype);
	}
	
	/**
	 * Deletes the specified property type from this ProductRepository
	 * @param ptypeID The property type SSID
	 * @return The deleted property type
	 */
	public PropertyType deletePropertyType(String ptypeID) {
		return propertyTypes.remove(ptypeID);
	}
}
