package com.cc.tree;

public enum MatchType {

	SINGLE,

	METHOD,

	CLASS,

	PACKAGE,

	ALL;

	public boolean isClass() {
		return this == CLASS;
	}

	public boolean isMethod() {
		return this == METHOD;
	}

	public boolean isPackage() {
		return this == PACKAGE;
	}

	public boolean isAll() {
		return this == ALL;
	}
}
