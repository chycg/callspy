package com.zeroturnaround.callspy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stack {

	private static boolean consoleLog;

	private static String indent = " ";

	private static String filePath;

	private static Map<Long, Trace> map = new ConcurrentHashMap<>();

	static void init(boolean consoleLog, String indent, String filePath) {
		Stack.consoleLog = consoleLog;
		Stack.indent = indent == null ? " " : indent;
		Stack.filePath = filePath;
	}

	public static void push() {
		long threadId = Thread.currentThread().getId();
		Trace trace = map.get(threadId);
		if (trace == null) {
			trace = new Trace(consoleLog, indent, threadId, filePath);
			map.put(threadId, trace);
		}

		trace.push();
	}

	public static void pop() {
		Trace trace = map.get(Thread.currentThread().getId());
		trace.pop();
	}

	public static void log(String string) {
		Trace trace = map.get(Thread.currentThread().getId());
		trace.log(string);
	}

	public static void push(String method, Object[] args) {
		push();
		log(method, args);
	}

	public static void log(String method, Object[] args) {
		log(method + "(" + Utils.toString(args) + ")");
	}

	public static void log(String method, Object[] args, Object returnValue) {
		log(method + "(" + Utils.toString(args) + ") -> " + returnValue);
	}
}
