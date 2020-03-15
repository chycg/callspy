package com.zeroturnaround.callspy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallSpy implements ClassFileTransformer {

	private Set<String> includes;
	private Set<String> excludes;
	private Set<String> excludeMethod;

	private boolean showEntry;
	private boolean showGetter;

	private String currentMethod;

	public CallSpy(String file) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(file);) {
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}

		includes = Utils.splitString(properties.getProperty("include"));
		excludes = Utils.splitString(properties.getProperty("exclude"));
		excludeMethod = Utils.splitString(properties.getProperty("excludeMethod"));

		String value = properties.getProperty("showEntry"); // 是否显示方法进入
		showEntry = Boolean.valueOf(value);

		value = properties.getProperty("showGetter");
		showGetter = Boolean.valueOf(value);

		value = properties.getProperty("consoleLog"); // 是否输出控制台日志
		boolean consoleLog = value == null ? true : Boolean.valueOf(value);

		String indent = properties.getProperty("indent");
		String path = properties.getProperty("filePath");
		if (path == null)
			path = "user.log";

		Stack.init(consoleLog, indent, path);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (className == null || className.startsWith("com/zeroturnaround/callspy"))
			return null;

		for (String e : excludes) {
			if (e.trim().isEmpty())
				continue;

			String name = className.replace('/', '.');
			String Name = null;
			if (e.charAt(0) >= 65 && e.charAt(0) <= 97) {
				int index = name.lastIndexOf('.');
				Name = name.substring(index + 1, name.length());
			}

			if (name.startsWith(e) || Name != null && e.endsWith(Name))
				return bytes;
		}

		ClassPool cp = ClassPool.getDefault();
		cp.importPackage("com.zeroturnaround.callspy");

		for (String s : includes) {
			if (className.replace('/', '.').startsWith(s)) {
				CtClass ct = null;
				try {
					ct = cp.makeClass(new ByteArrayInputStream(bytes));

					CtMethod[] declaredMethods = ct.getDeclaredMethods();
					for (CtMethod method : declaredMethods) {
						if (Modifier.isAbstract(method.getModifiers()))
							continue;

						String name = method.getName();
						if (excludeMethod.contains(name))
							continue;

						if (showGetter && method.getParameterTypes().length == 0 && (name.startsWith("get") || name.startsWith("is")))
							continue;

						currentMethod = className + "." + name;

						String before = showEntry ? "{ Stack.push(\"" + currentMethod + "\", $args);}" : "{Stack.push();}";

						method.insertBefore(before);

						String end = "{ Stack.log(\"" + currentMethod + "\", $args, $type == void.class? \"void\": String.valueOf($_)); Stack.pop(); }";

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
