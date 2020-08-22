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

	private Integer targetId = 0;

	public DataChangeEvent(Object source, int eventType, Integer targetId, Collection<? extends Element> targets) {
		super(source);
		this.targetId = targetId;
		this.eventType = eventType;
		this.elements = targets;
	}

	public int getEventType() {
		return eventType;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public Collection<? extends Element> getElements() {
		return elements;
	}
}
