package com.zeroturnaround.callspy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Properties;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallSpy implements ClassFileTransformer {

	private String includes = "";
	private String excludes = "";

	public CallSpy(String file) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(file);) {
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}

		includes = properties.getProperty("include");
		excludes = properties.getProperty("exclude");

		if (includes == null)
			includes = "";

		if (excludes == null)
			excludes = "";
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (className.startsWith("com/zeroturnaround/callspy")) {
			return null;
		}

		for (String e : excludes.split(",")) {
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

		for (String s : includes.split(",")) {
			if (className.replace('/', '.').startsWith(s)) {
				CtClass ct = null;
				try {
					ct = cp.makeClass(new ByteArrayInputStream(bytes));

					CtMethod[] declaredMethods = ct.getDeclaredMethods();
					for (CtMethod method : declaredMethods) {
						// method.insertBefore(printArgs(method));
						// System.out.println(getInvoke("Stack.log", className, method));
						// String logLine = "System.out.println(\"" + className + "." + method.getName() + "(\" +
						// Stack.toString($args) + \")\");";

						String before = " { " +

								"Stack.push();" +

								"Stack.log(\"" + className + "." + method.getName() + "(\" + Stack.toString($args) + \")\");"

								+ "}";

						method.insertBefore(before);
						method.insertAfter("{ Stack.pop(); }", true);
					}

					return ct.toBytecode();
				} catch (Throwable e) {
					e.printStackTrace();
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

		// String logLine = "System.out.println(\"" + className + "." + methodName + "(\" + Stack.toString($args) +
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
