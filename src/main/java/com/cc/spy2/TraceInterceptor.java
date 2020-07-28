package com.cc.spy2;

import java.io.FileInputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.cc.Stack;
import com.cc.Utils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class TraceInterceptor {

	static Set<String> includes;
	static Set<String> excludes;

	static Set<String> excludeClass;
	static Set<String> excludeMethod;

	static boolean showEntry;
	static boolean showGetter;

	static String currentMethod;

	static Set<String> imports;

	/**
	 * 显示方法类型而非参数值，默认false
	 */
	static boolean showArgType;

	/**
	 * 相同方法出现次数
	 */
	static int maxCount;

	static Map<String, AtomicInteger> countMap = new HashMap<>();

	/**
	 * @param args
	 * @param instrumentation
	 */
	static void init(String args, Instrumentation instrumentation) {
		parseArgs(args);

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

		Listener listener = new Listener() {
			@Override
			public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
			}

			@Override
			public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
			}

			@Override
			public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) {
			}

			@Override
			public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
			}
		};

		Junction<TypeDescription> nameStartsWith = null;
		for (String e : includes) {
			if (nameStartsWith == null)
				nameStartsWith = ElementMatchers.nameStartsWith(e);
			else
				nameStartsWith = nameStartsWith.or(ElementMatchers.nameStartsWith(e));
		}

		new AgentBuilder.Default().type(nameStartsWith).transform(transformer).with(listener).installOn(instrumentation);
	}

	/**
	 * @param args
	 */
	private static void parseArgs(String args) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(args);) {
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}

		includes = Utils.splitString(properties.getProperty("include"));
		excludes = Utils.splitString(properties.getProperty("exclude"));

		Set<String> packages = excludes.stream().filter(e -> !e.contains(".")).collect(Collectors.toSet());
		excludes.removeAll(packages);
		for (String e : includes) {
			for (String p : packages) {
				excludes.add(e + "." + p);
			}
		}

		excludeClass = Utils.splitString(properties.getProperty("excludeClass"));
		excludeMethod = Utils.splitString(properties.getProperty("excludeMethod"));

		String value = properties.getProperty("showEntry"); // 是否显示方法进入
		showEntry = Boolean.valueOf(value);

		value = properties.getProperty("showGetter");
		showGetter = Boolean.valueOf(value);

		value = properties.getProperty("maxCount", "200");
		maxCount = Integer.parseInt(value);

		value = properties.getProperty("consoleLog"); // 是否输出控制台日志
		boolean consoleLog = value == null ? true : Boolean.valueOf(value);

		String importsValue = properties.getProperty("imports");
		imports = Utils.splitString(importsValue);
		imports.add("com.cc");
		imports.add("net.bytebuddy");

		String indent = properties.getProperty("indent");
		String path = properties.getProperty("filePath");
		if (path == null)
			path = "user.log";

		Stack.init(consoleLog, indent, path);
	}

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] arguments) throws Exception {
		boolean need = needTrace(method);
		Class<?> clz = method.getDeclaringClass();
		String currentMethod = clz.getName() + "." + method.getName();

		Object[] args = showArgType ? method.getParameterTypes() : arguments;

		if (need) {
			if (showEntry) {
				Stack.push(currentMethod, args);
			} else {
				Stack.push();
			}
		}

		Object result = callable.call();
		if (need) {
			Stack.log(currentMethod, args, result);
			Stack.pop();
		}

		return result;
	}

	private static boolean needTrace(Method method) {
		String methodName = method.getName();

		if (!showGetter && method.getParameterTypes().length == 0 && (methodName.startsWith("get") || methodName.startsWith("is")))
			return false;

		String name = method.getDeclaringClass().getName();
		if (excludes.stream().anyMatch(e -> name.startsWith(e)))
			return false;

		int index = name.lastIndexOf('.');
		String Name = null;
		if (index > 0) {
			Name = name.substring(index + 1, name.length());
		}

		if (excludeClass.contains(Name))
			return false;

		if (excludeMethod.contains(methodName) || excludeMethod.contains(Name + "." + methodName))
			return false;

		String key = Name + "." + methodName;
		countMap.putIfAbsent(key, new AtomicInteger());
		int count = countMap.get(key).get();
		if (count > maxCount) {
			System.out.println(key + ": count=" + count);
			return false;
		}

		return includes.stream().anyMatch(e -> name.startsWith(e));
	}
}
