package com.cc.spy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.cc.Config;
import com.cc.Stack;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class TraceInterceptor {

	public static Config config;

	private static ThreadLocal<Map<Integer, Boolean>> showStatus = new ThreadLocal<>();

	private static ThreadLocal<AtomicInteger> currentLevel = new ThreadLocal<>();

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] arguments) throws Exception {
		Class<?> clz = method.getDeclaringClass();
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;
		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		if (currentLevel.get() == null) {
			currentLevel.set(new AtomicInteger(0));
		}

		if (showStatus.get() == null) {
			showStatus.set(new HashMap<>());
		}

		currentLevel.get().getAndIncrement();
		final int level = currentLevel.get().get();
		showStatus.get().put(level, true);

		Boolean parentShow = showStatus.get().getOrDefault(level - 1, true);
		boolean needTrace = parentShow && config.needTrace(method);
		showStatus.get().put(level, needTrace);

		if (needTrace) {
			Stack.push(currentMethod, args);
		}

		Object result = null;
		try {
			result = callable.call();
		} finally {
			if (needTrace) {
				Object resultValue = method.getReturnType() == void.class ? "void" : result;
				boolean hasLoop = Stack.hasLoop();

				if (hasLoop) {
					config.addLoopMethod(methodName);
					Stack.loopLog(currentMethod, args, resultValue);
				} else {
					Stack.log(currentMethod, args, resultValue);
				}

				Stack.pop();
			}

			showStatus.get().remove(level);
			currentLevel.get().getAndDecrement();
		}

		return result;
	}
}
