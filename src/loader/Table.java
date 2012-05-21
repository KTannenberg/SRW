package loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Table {
    public String name;
    public String[][] table;
    public int[][][] htable;
    public String[] states;
    public String[] events;

    public Map<Integer, String> hmodule;
    public Map<Integer, String> htables;

    public void setName(String name) {
        this.name = name;
    }

    public void setTable(String[][] table) {
        this.table = table;
        hmodule = new HashMap<Integer, String>();
        htables = new HashMap<Integer, String>();
        try {
            htable = new int[table.length][table[0].length][3];
            for (int n = 0; n < htable.length; n++) {
                for (int m = 0; m < htable[0].length; m++) {
                    String[] parts = table[n][m].split(" ");
                    htables.put(parts[0].hashCode(), parts[0]);
                    hmodule.put(parts[2].hashCode(), parts[2]);
                    htable[n][m][0] = parts[0].hashCode();
                    htable[n][m][1] = parts[1].hashCode();
                    htable[n][m][2] = parts[2].hashCode();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStates(String[] states) {
        this.states = states;
    }

    public void setEvents(String[] events) {
        this.events = events;
    }

    public int stateIndex(int stateID) {
        for (int n = 0; n < states.length; n++) {
            if (states[n].hashCode() == stateID)
                return n;
        }
        return -1;
    }

    public int eventIndex(int eventID) {
        for (int n = 0; n < events.length; n++) {
            if (events[n].hashCode() == eventID)
                return n;
        }
        return -1;
    }
}
