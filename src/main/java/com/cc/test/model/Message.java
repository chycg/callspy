package com.cc.test.model;

import java.io.Serializable;

import com.cc.test.util.ArrayUtils;

import lombok.Data;

@Data
public class Message implements Serializable {

	private static final long serialVersionUID = 1728898056816184326L;

	private static final String TAB = "\t";

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 执行时间
	 */
	private String time;

	/**
	 * 类名
	 */
	private String className;

	/**
	 * 方法名
	 */
	private String methodName;

	private String url;

	/**
	 * 当前执行结果，是否成功
	 */
	private boolean status;

	public Message(TestData e) {
		this.id = e.getId();
		this.time = ArrayUtils.getLongCurrentDate();
		this.className = e.getClassName();
		this.methodName = e.getMethodName();
		this.url = e.getUrl();
	}

	public String getInfo(boolean oldStatus) {
		boolean isExpected = status == oldStatus;
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(id).append("]").append(TAB).append(time).append(TAB);
		sb.append(String.format("%-100s", className + "." + methodName)).append(TAB);
		sb.append(String.format("%-30s", "success ( " + oldStatus + " --> " + status + " )"));
		sb.append(TAB).append(TAB).append(TAB).append(TAB);
		sb.append("[").append(isExpected ? "OK" : "ERR").append("]");

		return sb.toString();
	}
}
