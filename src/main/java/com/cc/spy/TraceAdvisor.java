package com.cc.spy;

import java.lang.reflect.Method;

import com.cc.Config;
import com.cc.InvokeStack;

import net.bytebuddy.asm.Advice;

public class TraceAdvisor {

	public static Config config;

	@Advice.OnMethodEnter
	public static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
		Class<?> clz = method.getDeclaringClass();
		int mod = method.getModifiers();
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;

		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		if (config.needTrace(method)) {
			if (config.isShowEntry()) {
				InvokeStack.push(mod, currentMethod, args);
			} else {
				InvokeStack.push();
			}
		}
	}

	@Advice.OnMethodExit
	public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments, @Advice.Return Object ret) {
		if (config.needTrace(method)) {
			int mod = method.getModifiers();
			String methodName = method.getName();
			String currentMethod = method.getDeclaringClass().getName() + "." + methodName;
			Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

			Object resultValue = method.getReturnType() == void.class ? "void" : ret;
			boolean hasLoop = InvokeStack.hasLoop();

			if (hasLoop) {
				config.addLoopMethod(methodName);
				InvokeStack.loopLog(mod, currentMethod, args, resultValue);
			} else {
				InvokeStack.log(mod, currentMethod, args, resultValue);
			}

			InvokeStack.pop();
		}
	}

}
