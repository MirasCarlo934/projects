package bm.main.engines;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import bm.jeep.ResError;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.tools.SystemTimer;

public abstract class AbstEngine extends TimerTask {
	private String logDomain;
	protected Logger LOG;
	protected Logger errorLOG;
	protected String name;
	private String className;
	protected LinkedList<EngineRequest> reqQueue = new LinkedList<EngineRequest>();
//	protected HashMap<String, Thread> threads = new HashMap<String, Thread>(10, 1);
	protected HashMap<String, Object> responses = new HashMap<String, Object>(10, 1);
//	/**
//	 * Contains the ERQS requests that need to wait for the response
//	 */
//	protected HashMap<EngineRequest, Boolean> waitFor = new HashMap<EngineRequest, Boolean>(10, 1);
	private Timer timer;
//	private SystemTimer sysTimer;
	private int counter = 0;
	private boolean processing = false; //true if the Engine is currently processing a request

	/**
	 * The current EngineRequest being processed by this Engine. Changes every time the <i>run()</i>
	 * method is invoked.
	 */
	protected EngineRequest currentRequest = null;
	//private SystemTimer systimer;
	
	public AbstEngine(String logDomain, String errorLogDomain, String name, String className/*, 
			SystemTimer sysTimer*/) {
		this.logDomain = logDomain;
		this.name = name;
		this.className = className;
//		this.sysTimer = sysTimer;
//		sysTimer.schedule(this, 0, 100);
		timer = new Timer(name);
		timer.schedule(this, 0, 100);
		LOG = Logger.getLogger(logDomain + "." + name);
		errorLOG = Logger.getLogger(errorLogDomain + "." + name);
	}
	
	/**
	 * Forwards and ERQS request to the specified engine. The request will be put into a queue and the engine will process
	 * the queued requests in a turn-per-turn basis. After processing, the engine will return a response for the forwarded
	 * request.
	 * 
	 * @param request The ERQS request for the specified engine to process
	 * @param engine The engine that will process the ERQS request
	 * @param logger The log4j logger used by the object that calls this method
	 * @param t The thread of the object that calls this method
	 * @param waitForResponse <b>True</b> if engine response <b><i>must</i></b> be returned. <b>False</b> if not. 
	 * 			<b>WARNING:</b> If this is false, this method will return a null value.
	 * @return The response of the engine to the request, or <b>null</b> if <i>waitForResponse</i> is set to false.
	 * @throws EngineException if the engine encounters an error while processing the request
	 */
	//TASK AbstEngine: Make the request handling simpler
	public Object forwardRequest(EngineRequest request, Thread t, boolean waitForResponse) 
			throws EngineException {
		request.setRequestingThread(t);
		request.setWaitForResponse(waitForResponse);
		LOG.trace("Adding " + request.getClass().getSimpleName() + " " + request.getSSID() + "!");
		reqQueue.add(request);
		if(waitForResponse) {
			synchronized(t){try {
				LOG.trace("Thread " + t.getName() + " set to wait");
				t.wait();
			} catch (InterruptedException e) {
				LOG.error("Thread " + t.getName() + " was interrupted! Cannot process " + request.getSSID(), e);
				throw new EngineException(this, "Thread " + t.getName() + 
						" was interrupted!", e);
			}}
			Object o = getResponse(request.getSSID());
			if(o.getClass().equals(EngineException.class)) {
				throw (EngineException) o; 
			} else {
				return o;
			}
		}
		return null;
	}
	
//	public static void forwardRequest(EngineRequest request, AbstEngine engine, Logger logger) throws EngineException {
//		engine.processRequest(request, Thread.currentThread());
//	}
	
	/**
	 * Processes the given EngineRequest.
	 * 
	 * @param engineRequest The EngineRequest
	 * @param t The Thread of the Object that called this method
	 * @return the response of the Engine, can be ResError if the Engine encountered an error or 
	 * 		if the EngineRequest is invalid
	 */
//	public void processRequest(EngineRequest engineRequest, Thread t) {
//		reqQueue.add(engineRequest);
////		threads.put(engineRequest.getId(), t);
//	}
	
	/**
	 * Retrieves the response from the specified EngineRequest and removes it from the Engine
	 * 
	 * @param engineRequestID The ID of the EngineRequest
	 * @return the response Object
	 */
	public Object getResponse(String engineRequestID) {
//		Iterator<String> ids = responses.keySet().iterator();
//		while(ids.hasNext()) {
//			LOG.fatal(ids.next());
//		}
		Object o = responses.remove(engineRequestID);
		LOG.trace("Returning response for request " + engineRequestID);
		return o;
	}
	
	@Override
	public void run() {
		if(!reqQueue.isEmpty() && processing == false) {
			counter++;
			EngineRequest er = reqQueue.removeFirst();
			LOG.trace("Processing EngineRequest " + er.getSSID() + "...");
			Thread processor = new Thread(new EngineProcessor(this, er, er.getRequestingThread()), 
					er.getRequestingThread().getName());
			synchronized(this) {
				processing = true;
			}
			processor.start();
		}
	}
	
	protected abstract Object processRequest(EngineRequest er);
	
	public String getLogDomain() {
		return logDomain;
	}

	/**
	 * The processing thread of the Engine. This object is instantiated every time an Engine
	 * needs to process an ERQS request (See ERQS document for request handling procedure).
	 * <br><br>
	 * This thread is actually not necessary, provided that the processing can take place
	 * within the TimerTask of the Engine. But for the sake of unclogging the Engine thread
	 * with multiple requests arriving at the same time, this was created so that the queuing
	 * thread (Timer thread) will not have to handle the processing which takes considerable
	 * amount of time. Instead, the processing takes place here, on a separate thread.
	 */
	private class EngineProcessor implements Runnable {
		AbstEngine engine;
		EngineRequest er;
		Thread parent;
		
		/**
		 * The processing thread of the Engine. This object is instantiated every time an Engine
		 * needs to process an ERQS request (See ERQS document for request handling procedure).
		 * <br><br>
		 * This thread is actually not necessary, provided that the processing can take place
		 * within the TimerTask of the Engine. But for the sake of unclogging the Engine thread
		 * with multiple requests arriving at the same time, this was created so that the queuing
		 * thread (Timer thread) will not have to handle the processing which takes considerable
		 * amount of time. Instead, the processing takes place here, on a separate thread.
		 */
		private EngineProcessor(AbstEngine engine, EngineRequest er, Thread parent) {
			this.er = er;
			this.parent = parent;
		}
		
		@Override
		public void run() {
			//checks if EngineRequest is valid for this Engine
			Thread.currentThread().setName(parent.getName() + "_back");
			boolean b = checkEngineRequest(er);
			Object res;
			if(b) {
				res = processRequest(er);
			} else {
				res = new EngineException(engine, "Invalid EngineRequest for " + name);
				LOG.error("Invalid EngineRequest for " + name);
			}
			LOG.trace("EngineRequest processing complete!");

			synchronized (this) {
				processing = false;
			}
			synchronized (parent) {
				if(er.waitForResponse() == true) {
					responses.put(er.getSSID(), res);
					
					parent.notifyAll();
					LOG.trace("Thread " + parent.getName() + " notified!");
				} else {
					if(res.getClass().equals(EngineException.class)) {
						EngineException e = (EngineException) res;
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}
		
		/**
		 * Checks if the EngineRequest is valid. Returns false if: <br>
		 * <ul>
		 * 		<li>This Engine cannot accommodate the given EngineRequest. (Wrong EngineRequest given)</li>
		 * </ul>
		 * @param er The EngineRequest
		 * @return <b><i>True</b></i> if the EngineRequest checks out, <b><i>false</b></i> otherwise.
		 */
		private boolean checkEngineRequest(EngineRequest er) {
			boolean b = false;
			
			if(er.getEngineType().equals(className)) {
				b = true;
			}
			
			return b;
		}
	}
}
