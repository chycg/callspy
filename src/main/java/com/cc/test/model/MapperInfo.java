package com.cc.test.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cc.test.util.ArrayUtils;

public class MapperInfo implements Serializable {

	private static final long serialVersionUID = 9024097670729684096L;

	private String name;

	private String className;

	private List<String> methods = new ArrayList<>();

	public MapperInfo() {
	}

	public MapperInfo(String name, String className) {
		this.name = name;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getMethods() {
		return methods;
	}

	public List<String> getMatched(String name) {
		if (ArrayUtils.isEmpty(name)) {
			return methods;
		}

		List<String> list = new ArrayList<>();
		for (String s : methods) {
			if (s.toLowerCase().indexOf(name) >= 0) {
				list.add(s);
			}
		}

		return list;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public void addMethod(String method) {
		methods.add(method);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
