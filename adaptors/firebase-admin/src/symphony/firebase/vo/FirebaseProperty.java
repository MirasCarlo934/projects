package symphony.firebase.vo;

import bm.context.properties.Property;
import org.apache.log4j.Logger;

public class FirebaseProperty {
	public String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIsGoogle() {
		return isGoogle;
	}
	public void setIsGoogle(String isGoogle) {
		this.isGoogle = isGoogle;
	}

	public Object value;
	public String name;
	public String isGoogle;
	private Logger logger = Logger.getLogger(FirebaseProperty.class);
	
	/*
	 * Default constructor
	 * This is needed so that data DataSnapshot.getValue(FirebaseProperty.class) will work
	 */
	public FirebaseProperty() {
		
	}
	/*
	 * Constructor to manually create a property
	 */
	public FirebaseProperty(String id, Object value) {
		this.id = id;
		this.value = value;
	}
	/*
	 * Constructor to create a property from the BM's device.properties
	 */
	public FirebaseProperty(Property props) {
		this.id = props.getSSID();
		this.value = props.getValue(); 
		this.name = props.getDisplayName();
		logger.info("property id="+id+" name="+name+" value="+value);
	}
}
