package com.zeroturnaround.callspy;

import java.util.Collection;

public class Stack {

	static String indent = "";

	public static void push() {
		indent += " ";
	}

	public static void pop() {
		indent = indent.substring(1);
	}

	public static void log(String string) {
		System.err.println(indent + string);
	}

	public static String toString(String[] args) {
		return toString((Object[]) args);
	}

	public static String toString(Object[] args) {
		if (args == null || args.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				sb.append(arg).append(",");
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	public static String toString(Collection<?> c) {
		if (c == null || c.isEmpty())
			return "";

		String splitChar = ",";

		StringBuilder sb = new StringBuilder(c.size() * 8);
		for (Object o : c)
			sb.append(o).append(splitChar);

		if (sb.length() > 0)
			sb.delete(sb.lastIndexOf(splitChar), sb.length());

		return sb.toString();
	}

}
