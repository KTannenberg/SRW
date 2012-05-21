package runtime;

import java.util.Map;

public class Scheduler implements Runnable {
	private volatile boolean running;
	
	private final int maxWorkers;
	private final int maxChannels;
	
	private Map<Integer, Channel> channels;
	private Map<Integer, Worker> workers;
	private Map<String, Table> tableset;
	private Map<Integer, Integer> allocation;

	public Scheduler(int maxWorkers, int maxChannels, Map<String, Table> tableset) {
		this.maxWorkers = maxWorkers;
		this.maxChannels = maxChannels;
		this.tableset = tableset;
	}

	@Override
	public void run() {
		while(running) {
			
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public synchronized void createChannel(String table) {
		
	}
	
	public synchronized void removeChannel(int chID) {
		
	}
	
	public synchronized void createWorker() {
		
	}
	
	public synchronized void removeWorker(int wrID) {
		
	}
	
	public synchronized void dispatchData(int chID, String name, Object data, String event) {
		
	}
	
	public synchronized void dispatchEvent(int chID, String event) {
		
	}
}
