package com.zeroturnaround.callspy.tree;

import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = 3358434219949751004L;

	private String line;

	private int count;

	private String method;

	private String result;

	public Node(String line, int count) {
		this.line = line.trim();
		this.count = count;
		this.method = line.substring(0, line.indexOf("("));
	}

	public String getMethod() {
		return method;
	}


	public String getMethodName() {
		return method.substring(method.indexOf(".") + 1);
	}

	public String getCallName() {
		return method.replaceAll("/", ".");
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

	public boolean isResult(String line) {
		if (hasResult() || line == null || line.trim().isEmpty())
			return false;

		return line.trim().startsWith(this.line);
	}

	@Override
	public String toString() {
		return line;
	}
}
