package runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import loader.ModuleLoader;
import loader.Module;
import loader.Table;
import loader.TableLoader;

public class Scheduler implements Runnable {
	private volatile boolean running;
	
	private final int maxWorkers;
	private final int maxChannels;
	private int curChannel;
	private int curWorker;
	
	private Map<Integer, Channel> channels;
	private Map<Integer, Worker> workers;
	private Map<Integer, Integer> allocation;
	
	private Map<String, Table> tableset;
	private Map<String, Module> modules;
	
	private ModuleLoader binloader;
	private TableLoader tableloader;
	

	public Scheduler(int workers, int channels, String tables, String classes) {
		maxWorkers = workers;
		maxChannels = channels;
		
		binloader = new ModuleLoader(classes, this.getClass().getClassLoader());
		tableloader = new TableLoader(tables);
	}

	@Override
	public void run() {
		while(running) {
			
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public synchronized void createChannel() throws SchedulerException {
		if(channels.size() < maxChannels) {
			channels.put(curChannel, new Channel());
			curChannel++;
		} else {
			throw new SchedulerException("SCHEDULER: No more channels available, destroy one of channels");
		}
	}
	
	public synchronized void removeChannel(int chID) throws SchedulerException {
		if(channels.containsKey(chID)) {
			channels.remove(chID);
		} else {
			throw new SchedulerException("SCHEDULER: Channel with ID " + chID + " not found");
		}
	}
	
	public synchronized void createWorker() throws SchedulerException {
		if(workers.size() < maxWorkers) {
			workers.put(curWorker, new Worker());
			curWorker++;
		} else {
			throw new SchedulerException("SCHEDULER: No more workers available, destroy one of workers");
		}
	}
	
	public synchronized void removeWorker(int wrID) throws SchedulerException {
		if(workers.containsKey(wrID)) {
			workers.remove(wrID);
		} else {
			throw new SchedulerException("SCHEDULER: Worker with ID " + wrID + " not found");
		}
	}
	
	public void inputData(int chID, String name, Object data, String event) throws SchedulerException {
		if(channels.containsKey(chID)) {
			if(event.equals("null")) event = null;
			channels.get(chID).inputData(name, data, event);
		} else {
			throw new SchedulerException("SCHEDULER: Channel with ID " + chID + " not found");
		}
	}
	
	public void createEvent(int chID, String event) throws SchedulerException {
		if(channels.containsKey(chID)) {
			if(event.equals("null")) event = null;
			channels.get(chID).createEvent(event);
		} else {
			throw new SchedulerException("SCHEDULER: Channel with ID " + chID + " not found");
		}
	}
	
	public void loadModule(String name) throws SchedulerException {
		if(modules.containsKey(name)) return;
		Module module = null;
		try {
			module = Module.class.cast(Class.forName(name, true, binloader));
		} catch (ClassNotFoundException e) {
			throw new SchedulerException("SCHEDULER: Error loading module " + name + " class not found");
		} catch (ClassCastException e) {
			throw new SchedulerException("SCHEDULER: Error loading module " + name + " cannot cast to interface 'Module'");
		}
		modules.put(name, module);
	}
	
	public void loadTable(String name) throws SchedulerException {
		if(tableset.containsKey(name)) return;
		Table table = null;
		try {
			table = tableloader.findTable(name);
		} catch (FileNotFoundException e) {
			throw new SchedulerException("SCHEDULER: Error loading table " + name + " file not found");
		} catch (IOException e) {
			throw new SchedulerException("SCHEDULER: Error loading table " + name + " no access to file");
		}
		tableset.put(name, table);
	}
}
