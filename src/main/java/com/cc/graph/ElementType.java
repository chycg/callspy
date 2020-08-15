package com.cc.graph;

public enum ElementType {

	NODE,

	LINE;

	public boolean isNode() {
		return this == NODE;
	}

	public boolean isLine() {
		return this == LINE;
	}

}
