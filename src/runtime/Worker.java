package runtime;

import loader.Module;
import loader.Table;

public class Worker implements Runnable {
    public int wrID;
    private long launchtime;
    private long timeframe;
    private Channel channel;

    private Object anchor;
    private Scheduler scheduler;

    private volatile boolean running;

    public Worker(Scheduler scheduler, Object anchor, int wrID) {
        this.scheduler = scheduler;
        this.anchor = anchor;
        this.wrID = wrID;
        timeframe = 10;
    }

    @Override
    public void run() {
        while (running) {
            if (launchtime + timeframe > System.currentTimeMillis()
                    && channel != null && channel.hasEvents()) {
                Table table = null;
                Module module = null;
                try {
                    table = scheduler.getTable(channel.getTable());
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out
                            .println("Worker and channel will be unbinded and terminated");
                    try {
                        scheduler.removeChannel(channel.chID);
                        scheduler.removeWorker(wrID);
                    } catch (SchedulerException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }

                try {
                    String event = channel.retrieveEvent();
                    int eventIndex = table.eventIndex(event.hashCode());
                    int stateIndex = table.stateIndex(channel.getState());

                    if (eventIndex < 0) {
                        throw new SchedulerException("Event '" + event
                                + "' is not handled by table '" + table.name
                                + "'");
                    }
                    if (stateIndex < 0) {
                        throw new SchedulerException("State with hash 0x"
                                + Integer.toHexString(channel.getState())
                                + " is not handled by table '" + table.name
                                + "'");
                    }

                    int[] element = table.htable[eventIndex][stateIndex];
                    
                    module = scheduler.getModule(element[2]);
                    for (String dataName : module.data()) {
                        if (channel.getData(dataName) == null)
                            throw new SchedulerException("Data block '"
                                    + dataName + "' is required by module '"
                                    + scheduler.getModuleName(element[2])
                                    + "' but not found in channel '"
                                    + channel.chID + "'");
                    }
                    for (Integer codeID : module.code().keySet()) {
                        if (table.eventIndex(codeID) == -1) {
                            throw new SchedulerException("Return code '"
                                    + module.code().get(codeID) + "' ("
                                    + codeID + ") isn't presented in table '"
                                    + table.name);
                        }
                    }
                    int result = module.run(channel);
                    if(result < 0) {
                        throw new SchedulerException(module.code().get(result));
                    } else if(result > 0) {
                        channel.createEvent(module.code().get(result));
                    }
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out
                            .println("Worker and channel will be unbinded and terminated");
                    try {
                        scheduler.removeChannel(channel.chID);
                        scheduler.removeWorker(wrID);
                    } catch (SchedulerException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }

                // working
            } else {
                synchronized (this) {
                    try {
                        this.wait();
                        launchtime = System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public synchronized void bind(Channel channel) {
        this.channel = channel;
        channel.wrID = wrID;
    }

    public synchronized void unbind() {
        this.channel.wrID = -1;
        channel = null;
        anchor.notify();
    }

    public boolean isBinded() {
        return channel != null;
    }

    public void stop() {
        running = false;
    }
}
