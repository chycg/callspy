package com.cc.spy2;

import static net.bytebuddy.matcher.ElementMatchers.isHashCode;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.isSetter;
import static net.bytebuddy.matcher.ElementMatchers.isToString;
import static net.bytebuddy.matcher.ElementMatchers.nameContainsIgnoreCase;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.cc.Config;
import com.cc.Stack;
import com.cc.Utils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

public class TraceInterceptor {

	private static Config config;

	/**
	 * 重复出现的方法与次数
	 */
	private static Map<String, AtomicInteger> countMap = new HashMap<>();

	/**
	 * 循环调用方法记录，循环方法不再记录日志
	 */
	private static Set<String> loopMethods = new HashSet<>();

	/**
	 * @param args
	 * @param instrumentation
	 */
	static void init(String args, Instrumentation instrumentation) {
		config = new Config(args);
		Stack.init(config);

		transform(instrumentation);
	}

	/**
	 * @param instrumentation
	 */
	private static void transform(Instrumentation instrumentation) {
		Transformer transformer = new Transformer() {
			@Override
			public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
				return builder.method(ElementMatchers.any()).intercept(MethodDelegation.to(TraceInterceptor.class));
			}
		};

		Junction<?> judge = new DefaultJunction<NamedElement>();
		judge = judge.and(not(isInterface())).and(not(isSetter())).and(not(isToString())).and(not(isHashCode()));

		for (String e : config.getIncludes()) {
			judge = judge.and(ElementMatchers.nameStartsWith(e));
		}

		for (String e : config.getExcludes()) {
			judge = judge.and(not(nameContainsIgnoreCase(e)));
		}

		for (String e : config.getExcludeMethod()) {
			judge = judge.and(not(ElementMatchers.nameEndsWith(e)));
		}

		new AgentBuilder.Default().type(new DefaultMatcher(judge)).transform(transformer).installOn(instrumentation);
	}

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] arguments) throws Exception {
		Class<?> clz = method.getDeclaringClass();
		String methodName = method.getName();
		String currentMethod = clz.getName() + "." + methodName;

		Object[] args = config.isShowMethodInfo() ? method.getParameterTypes() : arguments;

		boolean need = needTrace(method);
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
					loopMethods.add(methodName);
					Stack.loopLog(currentMethod, args, resultValue);
				} else {
					Stack.log(currentMethod, args, resultValue);
				}

				Stack.pop();
			}
		}

		return result;
	}

	private static boolean needTrace(Method method) {
		String methodName = method.getName();

		if (isBasicMethod(methodName) || loopMethods.contains(methodName))
			return false;

		if (!config.isShowGetter() && method.getParameterTypes().length == 0 && (methodName.startsWith("get") || methodName.startsWith("is")))
			return false;

		String name = method.getDeclaringClass().getName();
		if (config.getExcludes().stream().anyMatch(e -> name.startsWith(e)))
			return false;

		int index = name.lastIndexOf('.');
		String Name = null;
		if (index > 0) {
			Name = name.substring(index + 1, name.length());
		}

		if (config.getExcludeClass().contains(Name))
			return false;

		if (config.getExcludeMethod().contains(methodName) || config.getExcludeMethod().contains(Name + "." + methodName))
			return false;

		String key = Name + "." + methodName;
		countMap.putIfAbsent(key, new AtomicInteger());
		int count = countMap.get(key).get();
		if (count > config.getMaxCount()) {
			System.out.println(key + ": count=" + count);
			return false;
		}

		return config.getIncludes().stream().anyMatch(e -> name.startsWith(e));
	}

	private static boolean isBasicMethod(String methodName) {
		return Utils.isContain(methodName, "toString", "hashCode", "equals", "wait", "clone");
	}
}
