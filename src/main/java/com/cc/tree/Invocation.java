package com.cc.tree;

import java.io.Serializable;

import com.cc.Utils;

public class Invocation implements Serializable {

	private static final long serialVersionUID = 3358434219949751004L;

	private final int count;

	private final int mod;

	private final String method; // 完整路径

	private String line;

	private String result;

	public Invocation(String line, int count,int mod) {
		this.line = line.trim();
		this.count = count;
		this.method = line.substring(0, line.indexOf("("));
		this.mod = mod;
	}

	public String getMethod() {
		return method;
	}

	public String getMethodName() {
		return method.substring(method.lastIndexOf(".") + 1);
	}

	public String getClassName() {
		int dotIndex = method.lastIndexOf(".");
		int dot2Index = method.lastIndexOf(".", dotIndex - 1);
		return method.substring(dot2Index + 1, dotIndex);
	}

	public String getPackageName() {
		int dotIndex = method.lastIndexOf(".");
		int dot2Index = method.lastIndexOf(".", dotIndex - 1);
		int dot3Index = method.lastIndexOf(".", dot2Index - 1);
		return method.substring(dot3Index + 1, dot2Index);
	}

	public void setResultLine(String line) {
		line = line.trim();
		int index = line.indexOf("->");
		this.line = line;
		this.result = line.substring(index + 2).trim();
	}

	public boolean hasResult() {
		return result != null;
	}

	public String getLine() {
		return line;
	}

	public int getCount() {
		return count;
	}

	public int getMod() {
		return mod;
	}

	public boolean isResult(String line) {
		if (hasResult() || line == null || line.trim().isEmpty())
			return false;

		return line.trim().startsWith(this.line);
	}

	public boolean isMatch(String text, MatchType type) {
		if (type == null)
			return false;

		if (Utils.isEmpty(text) || type.isAll())
			return true;

		if (type.isMethod()) {
			return line.contains("." + text + "(");
		}

		if (type.isClass()) {
			return method.contains("." + text + ".") || method.contains("." + text + "$");
		}

		if (type.isPackage()) {
			return method.contains("." + text + ".");
		}

		return false;
	}

	@Override
	public String toString() {
		return line;
	}
}
