package com.cc.spy;

import java.lang.reflect.Method;

import com.cc.Config;
import com.cc.Stack;

import net.bytebuddy.asm.Advice;

public class TraceAdvisor {

	public static Config config;

	@Advice.OnMethodEnter
	public static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
		Class<?> clz = method.getDeclaringClass();
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;

		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		if (config.needTrace(method)) {
			if (config.isShowEntry()) {
				Stack.push(currentMethod, args);
			} else {
				Stack.push();
			}
		}
	}

	@Advice.OnMethodExit
	public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments, @Advice.Return Object ret) {
		if (config.needTrace(method)) {
			String methodName = method.getName();
			String currentMethod = method.getDeclaringClass().getName() + "." + methodName;
			Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

			Object resultValue = method.getReturnType() == void.class ? "void" : ret;
			boolean hasLoop = Stack.hasLoop();

			if (hasLoop) {
				config.addLoopMethod(methodName);
				Stack.loopLog(currentMethod, args, resultValue);
			} else {
				Stack.log(currentMethod, args, resultValue);
			}

			Stack.pop();
		}
	}

}
