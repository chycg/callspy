package com.cc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvokeStack {

	private static Config config;

	private static Map<Long, Trace> map = new ConcurrentHashMap<>();

	public static void init(Config config) {
		InvokeStack.config = config;
	}

	public static void push() {
		Trace trace = getTrace();

		if (trace == null) {
			long id = Thread.currentThread().getId();
			trace = new Trace(id, config.getPath());
			map.put(id, trace);
		}

		trace.push();
	}

	public static void push(int mod, String method, Object[] args) {
		push();

		if (config.isShowEntry())
			log(mod, method, args);
	}

	public static int getDepth() {
		Trace trace = getTrace();
		if (trace == null)
			return 0;

		return trace.getDepth();

	}

	/**
	 * 检测是否有循环调用，深度超出一定范围
	 * 
	 * @return
	 */
	public static boolean hasLoop() {
		Trace trace = getTrace();
		if (trace == null)
			return false;

		return trace.getDepth() > config.getMaxDepth();
	}

	public static void pop() {
		getTrace().pop();
	}

	/**
	 * @return
	 */
	private static Trace getTrace() {
		return map.get(Thread.currentThread().getId());
	}

	public static void log(int mod, String string) {
		Trace trace = getTrace();
		trace.write(mod, string);

		if (config.isShowConsoleLog())
			System.out.println(trace.getPrefix(mod) + string);
	}

	public static void log(int mod, String method, Object[] args) {
		log(mod, method + "(" + Utils.getArgs(args) + ")");
	}

	public static void log(int mod, String method, Object[] args, Object returnValue) {
		log(mod, method + "(" + Utils.getArgs(args) + ") -> " + Utils.toString(returnValue));
	}

	/**
	 * 循环调用日志记录，不记录方法入参与返回值，只记录类型
	 * 
	 * @param method
	 * @param args
	 * @param returnValue
	 */
	public static void loopLog(int mod, String method, Object[] args, Object returnValue) {
		if (returnValue == null)
			returnValue = "null";
		else
			returnValue = "<" + returnValue.getClass().getSimpleName() + ">";

		args = Utils.getArgTypes(args);
		log(mod, method, args, returnValue);
	}
}
