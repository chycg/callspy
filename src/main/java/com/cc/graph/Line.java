package com.cc.graph;

import java.io.Serializable;

public class Line implements Serializable {

	private static final long serialVersionUID = 1007906709323772156L;

	private final Node from;

	private final Node to;

	private final String method;

	public Line(Node from, Node to, String method) {
		this.from = from;
		this.to = to;
		this.method = method;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public String getMethod() {
		return method;
	}
}
