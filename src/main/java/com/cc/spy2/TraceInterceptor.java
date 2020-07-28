package com.cc.spy2;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.cc.Stack;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class TraceInterceptor {

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
		boolean need = needTrace(method);
		Class<?> clz = method.getDeclaringClass();
		String currentMethod = clz.getName() + "." + method.getName();

		if (need) {
			if (Agent.showEntry) {
				Stack.push(currentMethod, method.getParameterTypes());
			} else {
				Stack.push();
			}
		}

		Object o = callable.call();
		if (need) {
			Stack.log(currentMethod, method.getParameterTypes(), o);
			Stack.pop();
		}

		return o;
	}

	private static boolean needTrace(Method method) {
		String methodName = method.getName();

		if (!Agent.showGetter && method.getParameterTypes().length == 0 && (methodName.startsWith("get") || methodName.startsWith("is")))
			return false;

		String name = method.getDeclaringClass().getName();
		int index = name.lastIndexOf('.');
		String Name = null;
		if (index > 0) {
			Name = name.substring(index + 1, name.length());
		}

		if (Agent.excludeClass.contains(Name))
			return false;

		if (Agent.excludeMethod.contains(methodName) || Agent.excludeMethod.contains(Name + "." + methodName))
			return false;

		String key = Name + "." + methodName;
		Agent.countMap.putIfAbsent(key, new AtomicInteger());
		int count = Agent.countMap.get(key).get();
		if (count > Agent.maxCount) {
			System.out.println(key + ": count=" + count);
			return false;
		}

		for (String p : Agent.includes) {
			if (name.startsWith(p))
				return true;
		}

		return false;
	}
}
