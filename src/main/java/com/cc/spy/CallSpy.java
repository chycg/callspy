package com.cc.spy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.cc.Stack;
import com.cc.Utils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallSpy implements ClassFileTransformer {

	private Set<String> includes;
	private Set<String> excludes;

	private Set<String> excludeClass;
	private Set<String> excludeMethod;

	private boolean showEntry;
	private boolean showGetter;

	private String currentMethod;

	private Set<String> imports;

	/**
	 * 相同方法出现次数
	 */
	private int maxCount;

	private Map<String, AtomicInteger> countMap = new HashMap<>();

	public CallSpy(String file) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(file);) {
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
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (className == null || className.startsWith("com/cc/spy") || clazz != null && clazz.isInterface())
			return bytes;

		for (String e : excludes) {
			if (e.trim().isEmpty())
				continue;

			String name = className.replace('/', '.');
			int index = name.lastIndexOf('.');
			String Name = null;
			if (index > 0) {
				Name = name.substring(index + 1, name.length());
			}

			if (name.startsWith(e) || Name != null && (e.endsWith(Name) || e.contains(Name + "$") || excludeClass.contains(Name)))
				return bytes;
		}

		ClassPool cp = ClassPool.getDefault();
		for (String item : imports) {
			cp.importPackage(item);
		}

		for (String s : includes) {
			if (className.replace('/', '.').startsWith(s)) {
				CtClass ct = null;
				try {
					ct = cp.makeClass(new ByteArrayInputStream(bytes));

					CtMethod[] declaredMethods = ct.getDeclaredMethods();
					for (CtMethod method : declaredMethods) {
						if (Modifier.isAbstract(method.getModifiers()))
							continue;

						String methodName = method.getName();
						if (excludeMethod.contains(methodName)
								|| excludeMethod.contains(method.getDeclaringClass().getSimpleName() + "." + methodName))
							continue;

						if (!showGetter && method.getParameterTypes().length == 0 && (methodName.startsWith("get") || methodName.startsWith("is")))
							continue;

						countMap.putIfAbsent(methodName, new AtomicInteger());
						AtomicInteger counter = countMap.get(methodName);
						if (counter.getAndIncrement() > maxCount) {
							System.out.println(methodName + ": count=" + counter.intValue());
							continue;
						}

						currentMethod = className + "." + methodName;

						String before = showEntry ? "{ Stack.push(\"" + currentMethod + "\", $args);}" : "{Stack.push();}";

						method.insertBefore(before);

						String end = "{ Stack.log(\"" + currentMethod
								+ "\", $args, $type == void.class? \"void\": String.valueOf($_)); Stack.pop(); }";

						method.insertAfter(end, true);
					}

					return ct.toBytecode();
				} catch (CannotCompileException e) {
					e.printStackTrace();
					System.out.println("===== Class compile error: " + currentMethod);
				} catch (NotFoundException e) {
					System.out.println("===== Class not found error: " + currentMethod);
				} catch (Throwable e) {
					e.printStackTrace();
					System.out.println("===== error: className = " + currentMethod);
				} finally {
					if (ct != null) {
						ct.detach();
					}
				}
			}
		}

		return bytes;
	}
}
