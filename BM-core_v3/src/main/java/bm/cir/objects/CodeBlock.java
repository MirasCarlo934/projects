package bm.cir.objects;

public abstract class CodeBlock {
	private String comID;
	private int propIndex;
	private Object propValue;
	
	public CodeBlock(String comID, String comProperty, Object comValue) {
		setDeviceID(comID);
		setDeviceProperty(Integer.parseInt(comProperty));
		setPropValue(comValue);
	}
	
	/**
	 * Turns this CodeBlock into a CIR Script String.
	 */
	public abstract String toString();

	/**
	 * @return the comID
	 */
	public String getDeviceID() {
		return comID;
	}

	/**
	 * @param comID the comID to set
	 */
	public void setDeviceID(String comID) {
		this.comID = comID;
	}

	/**
	 * @return the comProperty
	 */
	public int getPropertyIndex() {
		return propIndex;
	}

	/**
	 * @param comProperty the comProperty to set
	 */
	public void setDeviceProperty(int comProperty) {
		this.propIndex = comProperty;
	}

	/**
	 * @return the value of the property
	 */
	public Object getPropertyValue() {
		return propValue;
	}

	/**
	 * @param comValue the comValue to set
	 */
	public void setPropValue(Object comValue) {
		this.propValue = comValue;
	}
}
