package bm.smarthome.interfaces;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;

public abstract class SmarthomeElement {
//	private static final Logger LOG = Logger.getLogger(SmarthomeElement.class);
	protected String SSID;
	/**
	 * The index of this SmarthomeElement in its container
	 */
	private int index;
	protected DBAdaptor mainDBAdaptor;
	protected OHAdaptor mainOHAdaptor;
	protected AbstAdaptor[] additionalAdaptors;
	protected AbstAdaptor[] adaptors;

	public SmarthomeElement(String SSID, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors) {
		this.SSID = SSID;
		this.mainDBAdaptor = dba;
		this.mainOHAdaptor = oha;
		this.additionalAdaptors = additionalAdaptors;
		Vector<AbstAdaptor> a;
		if(additionalAdaptors == null)
			a = new Vector<AbstAdaptor>(2);
		else {
			a = new Vector<AbstAdaptor>(additionalAdaptors.length + 2);
			a.addAll(Arrays.asList(additionalAdaptors));
		}
		a.add(dba);
		a.add(oha);
		this.adaptors = a.toArray(new AbstAdaptor[a.size()]);
	}
	
	public String getSSID() {
		return SSID;
	}

	public void setSSID(String SSID) {
		this.SSID = SSID;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public DBAdaptor getMainDBAdaptor() {
		return mainDBAdaptor;
	}
	
	public OHAdaptor getMainOHAdaptor() {
		return mainOHAdaptor;
	}

	public AbstAdaptor[] getAllAdaptors() {
		return adaptors;
	}

	/**
	 * Persists this object. The common objective of this method is to use all the injected adaptors 
	 * to persist this object to various external systems and services. <br><br>
	 * 
	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
	 * repositories. That is mainly done in Modules.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param waitUntilPersisted 
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void persist(String parentLogDomain, boolean waitUntilPersisted) throws AdaptorException;
	
	/**
	 * Persists this object. The common objective of this method is to use all the injected adaptors 
	 * to persist this object to various external systems and services, <b>with the exception of some 
	 * specified adaptors</b>. <br><br>
	 * 
	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
	 * repositories. That is mainly done in Modules.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param exceptions the adaptors where this object <b>WILL NOT</b> be persisted to
	 * @param waitUntilPersisted 
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void persist(String parentLogDomain, AbstAdaptor[] exceptions, boolean waitUntilPersisted) 
			throws AdaptorException;
	
	/**
	 * Persists this object to all plugged adaptors <b>EXCEPT</b> the DBAdaptor. This method is invoked mainly by 
	 * repositories that wish to update the records of the persisted objects in all the peripheral systems.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
//	public abstract void persistExceptDB(String parentLogDomain) throws AdaptorException;
	
	/**
	 * Deletes this object. The common objective of this method is to use all the injected adaptors 
	 * to delete this object to various external systems and services. <br><br>
	 * 
	 * <i><b>Note:</b> This method does NOT guarantee that this object will be deleted from the BusinessMachine
	 * repositories. That is mainly done in Modules.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param waitUntilDeleted
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void delete(String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException;
	
//	/**
//	 * Updates this object. The common objective of this method is to use all the injected adaptors 
//	 * to update this object to various external systems and services. <br><br>
//	 * 
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be updated from the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 * 
//	 * @param parentLogDomain the log domain of the object that called this method
//	 * @throws AdaptorException if an adaptor fails to update this object
//	 */
	/**
	 * Updates this object to all plugged adaptors of this component.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param waitUntilUpdated
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void update(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException;
	
	/**
	 * Updates this object to all plugged adaptors with the exception of some specified adaptors.
	 * 
	 * @param exceptions the adaptors where this object <b>WILL NOT</b> be persisted to
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param waitUntilUpdated
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException;
	
	/**
	 * Updates this object to all plugged adaptors with the exception of the specified adaptor classes. <b><i>NOTE:</b> 
	 * ALL adaptors that have classes that are the same with the specified adaptor classes exceptions will become exceptions!
	 * </i>
	 * 
	 * @param exceptions the adaptor classes where this object <b>WILL NOT</b> be persisted to
	 * @param parentLogDomain the log domain of the object that called this method
	 * @param waitUntilUpdated
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	public abstract void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException;
}
