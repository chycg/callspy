package com.cc.spy2;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.cc.Config;
import com.cc.Stack;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class TraceInterceptor {

	public static Config config;

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] arguments) throws Exception {
		Class<?> clz = method.getDeclaringClass();
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;
		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		boolean need = config.needTrace(method);
		if (need) {
			if (config.isShowEntry()) {
				Stack.push(currentMethod, args);
			} else {
				Stack.push();
			}
		}

		Object result = null;
		try {
			result = callable.call();
		} finally {
			if (need) {
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
		}

		return result;
	}

}
