package com.cc.graph.event;

import java.util.Collection;
import java.util.EventObject;

import com.cc.graph.Element;

public class SelectionEvent extends EventObject {

	private static final long serialVersionUID = 235642255849459645L;

	public static final int ADD_SELECTION = 1;

	public static final int REMOVE_SELECTION = 2;

	public static final int CLEAR_SELETION = 3;

	public static final int SET_SELETION = 4;

	private Collection<? extends Element> elements;

	private int eventType;

	public SelectionEvent(Object source, int eventType, Collection<? extends Element> targets) {
		super(source);
		this.eventType = eventType;
		this.elements = targets;
	}

	public Collection<? extends Element> getElements() {
		return elements;
	}

	public void setElements(Collection<? extends Element> elements) {
		this.elements = elements;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

}
