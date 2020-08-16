package com.cc.graph.event;

import java.util.EventObject;

public class DataChangeEvent extends EventObject {

	private static final long serialVersionUID = -3548933394099104781L;

	public DataChangeEvent(Object source) {
		super(source);
	}

}
