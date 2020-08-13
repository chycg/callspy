package com.cc.tree;

public enum MatchType {

	NONE,

	METHOD,

	CLASS,

	PACKAGE;

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
		return this == NONE;
	}
}
