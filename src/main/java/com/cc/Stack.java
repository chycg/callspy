package com.cc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stack {

	private static boolean consoleLog;

	private static String indent = "~";

	private static String filePath;

	private static int maxDepth = 50;

	private static Map<Long, Trace> map = new ConcurrentHashMap<>();

	public static void init(Config config) {
		String indent = config.getIndent();
		Stack.consoleLog = config.isConsoleLog();
		Stack.indent = indent == null ? "~" : indent;
		Stack.filePath = config.getPath();
		Stack.maxDepth = config.getMaxDepth();
		Utils.showParamType = config.isShowParamType();
		Utils.showJson = config.isShowJson();
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

	/**
	 * 检测是否有循环调用，深度超出一定范围
	 * 
	 * @return
	 */
	public static boolean hasLoop() {
		Trace trace = map.get(Thread.currentThread().getId());
		if (trace == null)
			return false;

		return trace.getDepth() > maxDepth;
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
		log(method + "(" + Utils.getArgs(args) + ")");
	}

	public static void log(String method, Object[] args, Object returnValue) {
		log(method + "(" + Utils.getArgs(args) + ") -> " + Utils.toString(returnValue));
	}

	/**
	 * 循环调用日志记录，不记录方法入参与返回值，只记录类型
	 * 
	 * @param method
	 * @param args
	 * @param returnValue
	 */
	public static void loopLog(String method, Object[] args, Object returnValue) {
		if (returnValue == null)
			returnValue = "null";
		else
			returnValue = "<" + returnValue.getClass().getSimpleName() + ">";

		args = Utils.getArgTypes(args);
		log(method, args, returnValue);
	}
}
