package runtime;

import java.util.Map;

import loader.Element;


public class Table {
	String name;
	Element[][] table;
	Map<Integer, String> states;
	Map<Integer, String> events;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Element[][] getTable() {
		return table;
	}
	public void setTable(Element[][] table) {
		this.table = table;
	}
	public Map<Integer, String> getStates() {
		return states;
	}
	public void setStates(Map<Integer, String> states) {
		this.states = states;
	}
	public Map<Integer, String> getEvents() {
		return events;
	}
	public void setEvents(Map<Integer, String> events) {
		this.events = events;
	}
}
