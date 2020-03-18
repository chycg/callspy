package com.zeroturnaround.callspy;

import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = 3358434219949751004L;

	private String line;

	private String method;

	private String result;

	public Node(String line) {
		this.line = line.trim();
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getResult() {
		return result;
	}

	public void setResultLine(String line) {
		line = line.trim();
		int index = line.indexOf("->");
		this.result = line.substring(index + 2, line.length()).trim();
	}

	public boolean hasResult() {
		return result != null;
	}

	public String getLine() {
		return line;
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
