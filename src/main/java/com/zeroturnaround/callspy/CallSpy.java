package com.zeroturnaround.callspy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallSpy implements ClassFileTransformer {

	private Set<String> includes = new HashSet<>();
	private Set<String> excludes = new HashSet<>();
	private Set<String> excludeMethod = new HashSet<>();

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
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (className == null)
			return bytes;

		if (className.startsWith("com/zeroturnaround/callspy")) {
			return null;
		}

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
						// method.insertBefore(printArgs(method));
						// System.out.println(getInvoke("Stack.log", className, method));
						// String logLine = "System.out.println(\"" + className + "." + method.getName()
						// + "(\" +
						// Stack.toString($args) + \")\");";

						if (Modifier.isAbstract(method.getModifiers()))
							continue;

						String name = method.getName();

						if (excludeMethod.contains(name))
							continue;

						if (method.getParameterTypes().length == 0 && (name.startsWith("get") || name.startsWith("is")))
							continue;

						String before = " { " +

								"Stack.push();" +

								"Stack.log(\"" + className + "." + name + "(\" + Utils.toString($args) + \")\");"

								+ "}";

						method.insertBefore(before);
						method.insertAfter("{ Stack.pop(); }", true);
					}

					return ct.toBytecode();
				} catch (CannotCompileException e) {
					System.out.println("===== Class compile error: " + className);
				} catch (NotFoundException e) {
					System.out.println("===== Class not found error: " + className);
				} catch (Throwable e) {
					e.printStackTrace();
					System.out.println("===== error: className = " + className);
				} finally {
					if (ct != null) {
						ct.detach();
					}
				}
			}
		}

		return bytes;
	}

	private String printArgs(CtMethod method) {
		StringBuilder sb = new StringBuilder("{");

		String className = method.getDeclaringClass().getName();
		String methodName = method.getName();

		// sb.append("java.util.List list = new java.util.ArrayList();");
		// try {
		// CtClass[] types = method.getParameterTypes();
		// for (int i = 0; i < types.length; i++) {
		// sb.append("list.add(String.valueOf(").append("$" + (i + 1)).append("));");
		// }
		// } catch (NotFoundException e) {
		// e.printStackTrace();
		// }

		sb.append("String methodInfo = \"" + className + "." + methodName + "\";");
		sb.append("String argValues = Stack.toString($args);");
		String logLine = "methodInfo + \"(\" + argValues + \")\"";

		// String logLine = "System.out.println(\"" + className + "." + methodName +
		// "(\" + Stack.toString($args) +
		// \")\");";

		sb.append("System.out.println(" + logLine + ");");

		sb.append("}");

		return sb.toString();
	}

	private String getInvoke(String callMethod, String className, CtMethod method) {
		StringBuilder sb = new StringBuilder("(");
		try {
			CtClass[] types = method.getParameterTypes();

			for (int i = 0; i < types.length; i++) {
				sb.append("$").append(i + 1).append(",");
			}

			if (types.length > 0)
				sb.deleteCharAt(sb.length() - 1);

		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		sb.append(")");

		return getInvoke(callMethod, className.replace('/', '.') + "." + method.getName() + sb.toString());
	}

	private String getInvoke(String method, Object... value) {
		StringBuilder sb = new StringBuilder(method).append("(");
		if (value != null) {
			for (Object v : value) {
				if (v instanceof String && !v.toString().startsWith("$")) {
					sb.append("\"").append(v).append("\"").append(",");
				} else {
					sb.append(v).append(",");
				}
			}

			if (value.length > 0)
				sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(");");

		System.out.println(sb);

		return sb.toString();
	}

}
