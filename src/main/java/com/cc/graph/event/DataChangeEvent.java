package com.cc.graph.event;

import java.util.Collection;
import java.util.EventObject;

import com.cc.graph.Element;

public class DataChangeEvent extends EventObject {

	private static final long serialVersionUID = -3548933394099104781L;

	public static final int ADD = 1;

	public static final int REMOVE = 2;

	private Collection<? extends Element> elements;

	private int eventType;

	public DataChangeEvent(Object source, int eventType, Collection<? extends Element> targets) {
		super(source);
		this.eventType = eventType;
		this.elements = targets;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public Collection<? extends Element> getElements() {
		return elements;
	}

	public void setElements(Collection<? extends Element> elements) {
		this.elements = elements;
	}
}
