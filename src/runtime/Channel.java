package runtime;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Channel {
    protected Map<String, Object> storage;
    protected Deque<String> events;
    public volatile int wrID;
    public int chID;

    protected volatile int table;
    protected volatile int state;
    private Scheduler scheduler;

    public Channel(Scheduler scheduler, Integer mainTable, Integer mainState, int chID) {
        this.scheduler = scheduler;
        wrID = -1;
        this.chID = chID;
        storage = new HashMap<String, Object>();
        events = new LinkedList<String>();
        table = mainTable;
        state = mainState;
    }

    public synchronized void inputData(String name, Object data, String event) {
        storage.put(name, data);
        if (event != null) {
            createEvent(event);
        }
    }

    public synchronized Object getData(String name) {
        return storage.get(name);
    }

    public synchronized void setData(String name, Object data) {
        storage.put(name, data);
    }

    public synchronized void removeData(String name) {
        storage.remove(name);
    }

    public synchronized void createEvent(String event) {
        if(event == null) return;
        events.addLast(event);
    }

    public synchronized String retrieveEvent() {
        return events.pollFirst();
    }

    public synchronized boolean hasEvents() {
        return events.size() > 0;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int tableID) {
        table = tableID;
    }

    public int getState() {
        return state;
    }

    public void setState(int stateID) {
        state = stateID;
    }
}
