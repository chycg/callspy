package com.cc.test.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author chenyong
 * @date 2018年11月13日
 */
public class ClassInfo implements Serializable {

	private static final long serialVersionUID = 204144132607182644L;

	private String name;

	private List<String> methods = new ArrayList<>();

	private int access;

	public ClassInfo() {
	}

	public ClassInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void addChild(String name) {
		methods.add(name);
		methods.sort(Comparator.naturalOrder());
	}

	public int getAccess() {
		return access;
	}

	public void clearAccess() {
		this.access = 0;
	}

	public void addAccess() {
		this.access++;
	}

	@Override
	public String toString() {
		return "class:" + name + ", access:" + access;
	}

}
