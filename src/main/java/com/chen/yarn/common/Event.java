package com.chen.yarn.common;

public class Event {
	private EventType type;
	public enum EventType{
		TEST,
		TEST2
	}
	public Event(EventType type){
		this.type = type;
	}
	public EventType getType() {
		return type;
	}
}
