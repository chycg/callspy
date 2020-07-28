package com.cc.spy2;

import java.io.FileInputStream;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.cc.Stack;
import com.cc.Utils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class Agent {

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

	public static void premain(String args, Instrumentation instrumentation) {
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

		String indent = properties.getProperty("indent");
		String path = properties.getProperty("filePath");
		if (path == null)
			path = "user.log";

		Stack.init(consoleLog, indent, path);

		init(instrumentation);
	}

	/**
	 * @param args
	 * @param instrumentation
	 */
	private static void init(Instrumentation instrumentation) {
		AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
			@Override
			public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
				return builder.method(ElementMatchers.any()).intercept(MethodDelegation.to(TraceInterceptor.class));
			}
		};

		AgentBuilder.Listener listener = new AgentBuilder.Listener() {
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
}
