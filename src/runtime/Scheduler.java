package runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
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

    public Scheduler(int workers, int channels, String tables, String classes,
            String mainTable, String mainState) {
        maxWorkers = workers;
        maxChannels = channels;
        this.mainTable = mainTable.hashCode();
        this.mainState = mainState.hashCode();
    
        binloader = new ModuleLoader(classes, this.getClass().getClassLoader());
        tableloader = new TableLoader(tables);
        
        this.channels = new HashMap<Integer, Channel>(); 
        this.workers = new HashMap<Integer, Worker>();
        this.tables = new HashMap<String, Table>();
        this.modules = new HashMap<String, Module>();
        this.htables = new HashMap<Integer, String>();
        this.hmodule = new HashMap<Integer, String>();
        
        htables.put(mainTable.hashCode(), mainTable);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            for (Integer cursor : channels.keySet()) {
                Channel channel = channels.get(cursor);
                if (channel.hasEvents()) {
                    for (Integer cursor2 : workers.keySet()) {
                        Worker worker = workers.get(cursor2);
                        if (!worker.isBinded()) {
                            worker.bind(channel);
                        }
                    }
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        for(Worker worker : workers.values()) {
            worker.unbind();
            worker.stop();
        }
        workers.clear();
        channels.clear();
        running = false;
    }

    public synchronized void createChannel() throws SchedulerException {
        if (channels.size() < maxChannels) {
            channels.put(curChannel, new Channel(this, mainTable, mainState,
                    curChannel));
            curChannel++;
        } else {
            throw new SchedulerException(
                    "SCHEDULER: No more channels available, destroy one of channels");
        }
    }

    public synchronized void removeChannel(int chID) throws SchedulerException {
        if (channels.containsKey(chID)) {
            Channel channel = channels.remove(chID);
            if (channel.wrID > -1) {
                workers.get(channel.wrID).unbind();
            }
        } else {
            throw new SchedulerException("SCHEDULER: Channel with ID " + chID
                    + " not found");
        }
    }

    public synchronized void createWorker() throws SchedulerException {
        if (workers.size() < maxWorkers) {
            Worker worker = new Worker(this, curWorker);
            workers.put(curWorker, worker);
            curWorker++;
            new Thread(worker).start();
        } else {
            throw new SchedulerException(
                    "SCHEDULER: No more workers available, destroy one of workers");
        }
    }

    public synchronized void removeWorker(int wrID) throws SchedulerException {
        if (workers.containsKey(wrID)) {
            Worker worker = workers.remove(wrID);
            worker.unbind();
            worker.stop();
        } else {
            throw new SchedulerException("SCHEDULER: Worker with ID " + wrID
                    + " not found");
        }
    }

    public void inputData(int chID, String name, Object data, String event)
            throws SchedulerException {
        if (channels.containsKey(chID)) {
            if(channels.get(chID).wrID != -1) {
                throw new SchedulerException("SCHEDULER: Channel with ID " + chID
                        + " is in use, cannot add data to it now");
            } else {
                channels.get(chID).inputData(name, data, event);
            }
        } else {
            throw new SchedulerException("SCHEDULER: Channel with ID " + chID
                    + " not found");
        }
    }

    public void createEvent(int chID, String event) throws SchedulerException {
        if (channels.containsKey(chID)) {
            channels.get(chID).createEvent(event);
        } else {
            throw new SchedulerException("SCHEDULER: Channel with ID " + chID
                    + " not found");
        }
    }

    public Module loadModule(String name) throws SchedulerException {
        Module module = null;
        try {
            ///
            Object modu = Class.forName(name, true, binloader).newInstance();
            module = Module.class.cast(modu);
        } catch (ClassNotFoundException e) {
            throw new SchedulerException("SCHEDULER: Error loading module "
                    + name + " class not found");
        } catch (ClassCastException e) {
            throw new SchedulerException("SCHEDULER: Error loading module "
                    + name + " cannot cast to interface 'Module'");
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return modules.put(name, module);
    }

    public Table loadTable(String name) throws SchedulerException {
        Table table = null;
        try {
            table = tableloader.findTable(name);
        } catch (FileNotFoundException e) {
            throw new SchedulerException("SCHEDULER: Error loading table "
                    + name + " file not found");
        } catch (IOException e) {
            throw new SchedulerException("SCHEDULER: Error loading table "
                    + name + " no access to file");
        }
        hmodule.putAll(table.hmodule);
        htables.putAll(table.htables);
        table.hmodule.clear();
        table.htables.clear();
        return tables.put(name, table);
    }

    public Module getModule(int moduleID) throws SchedulerException {
        String name = getModuleName(moduleID);
        if (modules.containsKey(name)) {
            return modules.get(name);
        } else {
            return loadModule(name);
        }
    }

    public Table getTable(int tableID) throws SchedulerException {
        String name = getTableName(tableID);
        if (tables.containsKey(name)) {
            return tables.get(name);
        } else {
            return loadTable(name);
        }
    }

    public String getModuleName(int moduleID) {
        return hmodule.get(moduleID);
    }

    public String getTableName(int tableID) {
        return htables.get(tableID);
    }
    
    public String getState() {
        String state = "Workers: " + workers.size() + "/" + maxWorkers + "\n" + "Channels: " + channels.size() + "/" + maxChannels + "\n";
        String wrkrs = "";
        for(Worker worker : workers.values()) {
            wrkrs += "Worker " + worker.wrID + " is working: " + worker.isBinded() + "\n";
        }
        String chnnls = "";
        for(Channel channel : channels.values()) {
            chnnls += "Channels " + channel.chID + " is processed by " + channel.wrID + ", have events: " + channel.hasEvents() + "\n";
        }
        return state + wrkrs + chnnls;
    }
}
