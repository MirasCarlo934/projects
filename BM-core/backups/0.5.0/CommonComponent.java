package components;

import java.util.Vector;

import org.json.JSONObject;

import components.interfaces.OHItem;
import components.products.CommonProduct;
import components.properties.AbstProperty;
import components.properties.PropertyMode;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;

public class CommonComponent extends AbstComponent implements OHItem {

	public CommonComponent(String SSID, String MAC, String name, String topic, String room, boolean active,
			CommonProduct product, DBAdaptor dba, OHAdaptor oha) {
		super(SSID, MAC, name, topic, room, active, product, dba, oha);
	}
	
	/**
	 * Converts a component into JSON format. Also includes the component's properties in the 
	 * conversion
	 * 
	 * @param c The <i>AbstComponent</i> to be converted
	 * @return An array of <i>JSONObjects</i> containing the <i>AbstComponent's</i> JSON 
	 * 		representation
	 */
	@Override
	public JSONObject[] convertToItemsJSON() {
		//LOG.trace("Converting component " + c.getSSID() + " to OpenHAB items JSON...");
		Vector<JSONObject> itemsJSON = new Vector<JSONObject>(getProperties().length, 1);
		
		if(getProperties().length > 1) {
			//LOG.trace("Component has multiple properties! Creating group item first...");
			JSONObject json = new JSONObject();
			json.put("type", "Group");
			json.put("name", getSSID());
			json.put("label", getName());
			json.put("groupNames", new String[]{getRoom()});
			json.put("category", product.getOHIcon());
			itemsJSON.add(json);
		}
		
		AbstProperty[] props = getProperties();
		for(int i = 0; i < props.length; i++) {
			JSONObject json = new JSONObject();
			AbstProperty p = props[i];
			//LOG.trace("Converting property " + p.getSSID() + " to JSON...");
			if(p.getMode().equals(PropertyMode.Null)) 
				continue; //terminates here!!!
			String itemName = getSSID() + "_" + p.getSSID();
			String label = null;
			String room = null;
			String icon = null;
			String type = null;
			if(p.getMode().equals(PropertyMode.I))
				type = "String";
			else
				type = p.getOHItem();
			//System.out.println(type);
			if(getProperties().length > 1) {
				label = p.getDisplayName();
				room = getSSID();
			} else {
				label = getName();
				room = getRoom();
				icon = getProduct().getOHIcon();
			}
			json.put("name", itemName);
			json.put("type", type);
			json.put("label", label);
			json.put("category", icon);
			json.put("groupNames", new String[]{room});
			itemsJSON.add(json);
		}
		
		//LOG.trace("Conversion complete!");
		return itemsJSON.toArray(new JSONObject[itemsJSON.size()]);
	}
}
