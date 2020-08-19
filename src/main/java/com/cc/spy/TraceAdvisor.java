package com.cc.spy;

import java.lang.reflect.Method;

import com.cc.Config;
import com.cc.InvokeStack;
import com.cc.tree.Mod;

import net.bytebuddy.asm.Advice;

public class TraceAdvisor {

	public static Config config;

	@Advice.OnMethodEnter
	public static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
		Class<?> clz = method.getDeclaringClass();
		Mod mod = Mod.getByModifier(method.getModifiers());
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;

		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		if (config.needTrace(method, mod)) {
			InvokeStack.push(mod, currentMethod, args);
		}
	}

	@Advice.OnMethodExit
	public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments, @Advice.Return Object ret) {
		Mod mod = Mod.getByModifier(method.getModifiers());
		if (config.needTrace(method, mod)) {
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
