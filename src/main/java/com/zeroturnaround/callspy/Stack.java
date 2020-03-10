package com.zeroturnaround.callspy;

public class Stack {

	static String indent = "";

	public static void push() {
		indent += " ";
	}

	public static void pop() {
		if (indent.isEmpty())
			return;

		indent = indent.substring(1);
	}

	public static void log(String string) {
		System.out.println(indent + string);
	}
}
