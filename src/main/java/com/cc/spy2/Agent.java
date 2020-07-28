package com.cc.spy2;

import java.lang.instrument.Instrumentation;

public class Agent {

	public static void premain(String args, Instrumentation instrumentation) {
		System.out.println("Agent.premain, args = " + args);

		TraceInterceptor.init(args, instrumentation);
	}
}
