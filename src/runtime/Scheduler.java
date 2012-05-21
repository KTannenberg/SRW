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
	private int mainTable;
	private int mainState;
	
	private Map<Integer, Channel> channels;
	private Map<Integer, Worker> workers;
	
	private Map<String, Table> tables;
	private Map<String, Module> modules;
	

	private Map<Integer, String> htables;
	private Map<Integer, String> hmodule;
	
	private ModuleLoader binloader;
	private TableLoader tableloader;
	
	private Object anchor;

	public Scheduler(int workers, int channels, String tables, String classes, String mainTable, String mainState) {
		maxWorkers = workers;
		maxChannels = channels;
		this.mainTable = mainTable.hashCode();
		this.mainState = mainState.hashCode();
		this.anchor = new Object();
		
		binloader = new ModuleLoader(classes, this.getClass().getClassLoader());
		tableloader = new TableLoader(tables);
	}

	@Override
	public void run() {
		while(running) {
			for(Integer cursor : channels.keySet()) {
				Channel channel = channels.get(cursor);
				if(channel.hasEvents()) {
					for(Integer cursor2 : workers.keySet()) {
						Worker worker = workers.get(cursor2);
						if(worker.isBinded()) {
							worker.bind(channel);
						}
					}
				}
			}
			
			synchronized(anchor) {
				try {
					anchor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public synchronized void createChannel() throws SchedulerException {
		if(channels.size() < maxChannels) {
			channels.put(curChannel, new Channel(anchor, mainTable, mainState, curChannel));
			curChannel++;
		} else {
			throw new SchedulerException("SCHEDULER: No more channels available, destroy one of channels");
		}
	}
	
	public synchronized void removeChannel(int chID) throws SchedulerException {
		if(channels.containsKey(chID)) {
			Channel channel = channels.remove(chID);
			if(channel.wrID > -1) {
				workers.get(channel.wrID).unbind();
			}
		} else {
			throw new SchedulerException("SCHEDULER: Channel with ID " + chID + " not found");
		}
	}
	
	public synchronized void createWorker() throws SchedulerException {
		if(workers.size() < maxWorkers) {
			workers.put(curWorker, new Worker(this, anchor, curWorker));
			curWorker++;
		} else {
			throw new SchedulerException("SCHEDULER: No more workers available, destroy one of workers");
		}
	}
	
	public synchronized void removeWorker(int wrID) throws SchedulerException {
		if(workers.containsKey(wrID)) {
			Worker worker = workers.remove(wrID);
			worker.unbind();
			worker.stop();
			worker.notify();
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
	
	public Module loadModule(String name) throws SchedulerException {
		Module module = null;
		try {
			module = Module.class.cast(Class.forName(name, true, binloader));
		} catch (ClassNotFoundException e) {
			throw new SchedulerException("SCHEDULER: Error loading module " + name + " class not found");
		} catch (ClassCastException e) {
			throw new SchedulerException("SCHEDULER: Error loading module " + name + " cannot cast to interface 'Module'");
		}
		return modules.put(name, module);
	}
	
	public Table loadTable(String name) throws SchedulerException {
		Table table = null;
		try {
			table = tableloader.findTable(name);
		} catch (FileNotFoundException e) {
			throw new SchedulerException("SCHEDULER: Error loading table " + name + " file not found");
		} catch (IOException e) {
			throw new SchedulerException("SCHEDULER: Error loading table " + name + " no access to file");
		}
		hmodule.putAll(table.hmodule);
		htables.putAll(table.htables);
		table.hmodule.clear();
		table.htables.clear();
		return tables.put(name, table);
	}
	
	public Module getModule(int moduleID) throws SchedulerException {
		if(modules.containsKey(moduleID)) {
			return modules.get(moduleID);
		} else {
			return loadModule(getModuleName(moduleID));
		}
	}
	
	public Table getTable(int tableID) throws SchedulerException {
		if(tables.containsKey(tableID)) {
			return tables.get(tableID);
		} else {
			return loadTable(getTableName(tableID));
		}
	}
	
	public String getModuleName(int moduleID) {
		return hmodule.get(moduleID);
	}
	
	public String getTableName(int tableID) {
		return htables.get(tableID);
	}
}
