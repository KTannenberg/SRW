package runtime;

import loader.Module;
import loader.Table;

public class Worker implements Runnable {
    public int wrID;
    private long launchtime;
    private long timeframe;
    private Channel channel;

    private Scheduler scheduler;

    private volatile boolean running;

    public Worker(Scheduler scheduler, int wrID) {
        this.scheduler = scheduler;
        this.wrID = wrID;
        timeframe = 25;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            if (launchtime + timeframe > System.currentTimeMillis()
                    && channel != null && channel.hasEvents()) {
                Table table = null;
                Module module = null;
                try {
                    table = scheduler.getTable(channel.getTable());
                    table = scheduler.getTable(channel.getTable());
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out
                            .println("WORKER"
                                    + wrID
                                    + ": Worker and channel will be unbinded and terminated");
                    try {
                        scheduler.removeChannel(channel.chID);
                        scheduler.removeWorker(wrID);
                    } catch (SchedulerException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
                System.out.println("WORKER" + wrID + " is binded to CHANNEL"
                        + channel.chID + ", which has " + channel.events.size()
                        + " events scheduled. Working with table '"
                        + table.name + "'");
                try {
                    String event = channel.retrieveEvent();
                    int eventIndex = table.eventIndex(event.hashCode());
                    int stateIndex = table.stateIndex(channel.getState());

                    if (eventIndex < 0) {
                        throw new SchedulerException("WORKER" + wrID
                                + ": Event '" + event
                                + "' is not handled by table '" + table.name
                                + "'");
                    }
                    if (stateIndex < 0) {
                        throw new SchedulerException("WORKER" + wrID
                                + ": State with hash 0x"
                                + Integer.toHexString(channel.getState())
                                + " is not handled by table '" + table.name
                                + "'");
                    }
                    System.out.println("WORKER" + wrID + ": Current state is '"
                            + table.states[stateIndex]
                            + "', current event is '" + event + "'");
                    int[] element = table.htable[eventIndex][stateIndex];

                    if (element[2] != "$null".hashCode()) {
                        System.out.println("WORKER" + wrID
                                + ": Working with module '"
                                + scheduler.getModuleName(element[2]) + "'");
                        module = scheduler.getModule(element[2]);
                        module = scheduler.getModule(element[2]);
                        for (String dataName : module.data()) {
                            if (channel.getData(dataName) == null)
                                throw new SchedulerException("WORKER" + wrID
                                        + ": Data block '" + dataName
                                        + "' is required by module '"
                                        + scheduler.getModuleName(element[2])
                                        + "' but not found in channel '"
                                        + channel.chID + "'");
                        }
                        for (Integer codeID : module.code().keySet()) {
                            if (codeID > 0
                                    && table.eventIndex(module.code()
                                            .get(codeID).hashCode()) == -1) {
                                throw new SchedulerException("WORKER" + wrID
                                        + ": Return code '"
                                        + module.code().get(codeID) + "' ("
                                        + codeID + ") of module '"
                                        + scheduler.getModuleName(element[2])
                                        + "' isn't presented in table '"
                                        + table.name);
                            }
                        }
                        int result = module.run(channel.storage);
                        if (result < 0) {
                            throw new SchedulerException(module.code().get(
                                    result));
                        } else if (result > 0) {
                            String nevent = module.code().get(result);
                            System.out.println("WORKER" + wrID
                                    + ": Adding new event to channel '"
                                    + nevent + "'");
                            channel.createEvent(nevent);
                        }
                    } else {
                        System.out.println("WORKER" + wrID
                                + ": Module isn't specified ");
                    }
                    channel.setTable(element[0]);
                    channel.setState(element[1]);

                    System.out
                            .println("WORKER"
                                    + wrID
                                    + ": Going to table '"
                                    + scheduler.getTableName(element[0])
                                    + "' to state '"
                                    + (table.table[eventIndex][stateIndex])
                                            .split(" ")[1] + "' (" + element[1]
                                    + ")");
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out
                            .println("WORKER"
                                    + wrID
                                    + ": Worker and channel will be unbinded and terminated");
                    try {
                        scheduler.removeChannel(channel.chID);
                        scheduler.removeWorker(wrID);
                    } catch (SchedulerException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
            } else {
                try {
                    unbind();
                    Thread.sleep(25);
                    launchtime = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void bind(Channel channel) {
        this.channel = channel;
        channel.wrID = wrID;
    }

    public synchronized void unbind() {
        if (channel != null) {
            channel.wrID = -1;
        }
        channel = null;
    }

    public boolean isBinded() {
        return channel != null;
    }

    public void stop() {
        running = false;
    }
}
