package runtime;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Channel {
	private Map<String, Object> storage;
	private Deque<String> events;
	
	public Channel() {
		storage = new HashMap<String, Object>();
		events = new LinkedList<String>();
		storage.put("$table", "main");
		storage.put("$state", 0);
	}
	
	public synchronized void inputData(String name, Object data, String event) {
		storage.put(name, data);
		if(event != null) {
			createEvent(event);
		}
	}
	
	public synchronized void createEvent(String event) {
		events.addLast(event);
	}
	
	public synchronized String retrieveEvent() {
		return events.pollFirst();
	}
}
