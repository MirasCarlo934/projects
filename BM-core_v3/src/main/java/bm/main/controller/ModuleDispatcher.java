package bm.main.controller;

import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import bm.main.modules.SimpleModule;

public class ModuleDispatcher implements Runnable {
	private Logger LOG;
	private LinkedList<SimpleModule> moduleQueue;
	private ThreadPool threadPool;
	private int processCounter = 1;

	public ModuleDispatcher(String logDomain, LinkedList<SimpleModule> moduleQueue, ThreadPool threadPool) {
		LOG = Logger.getLogger(logDomain + "." + ModuleDispatcher.class.getSimpleName());
		this.moduleQueue = moduleQueue;
		this.threadPool = threadPool;
		LOG.info("ModuleDispatcher started!");
	}

	@Override
	public void run() {
		boolean waiting = false; //true if dispatcher is waiting for a thread to open
		while(!Thread.currentThread().isInterrupted()) {
			SimpleModule m = moduleQueue.poll();
			while(m != null) {
				try {
					threadPool.execute(m);
					LOG.trace("Executing " + m.getClass().getSimpleName() + " (RRN:" + processCounter + ")...");
					processCounter++;
					waiting = false;
					break;
				} catch(RejectedExecutionException e) {
					if(waiting == false) {
						LOG.trace("Waiting for a thread to open...");
						waiting = true;
					}
				}
			}
		}
	}
}
