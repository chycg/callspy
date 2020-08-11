package com.cc.spy;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.cc.Config;
import com.cc.Stack;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallSpy implements ClassFileTransformer {

	private Config config;

	private String currentMethod;

	private Map<String, AtomicInteger> countMap = new HashMap<>();

	public CallSpy(String file) {
		config = new Config(file);
		Stack.init(config);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (className == null || className.startsWith("com/cc/spy") || clazz != null && clazz.isInterface())
			return bytes;

		for (String e : config.getExcludes()) {
			if (e.trim().isEmpty())
				continue;

			String name = className.replace('/', '.');
			int index = name.lastIndexOf('.');
			String Name = null;
			if (index > 0) {
				Name = name.substring(index + 1, name.length());
			}

			if (name.startsWith(e) || Name != null && (e.endsWith(Name) || e.contains(Name + "$")))
				return bytes;
		}

		ClassPool cp = ClassPool.getDefault();
		for (String item : config.getImports()) {
			cp.importPackage(item);
		}

		if (className.replace('/', '.').startsWith(config.getIncludes())) {
			CtClass ct = null;
			try {
				ct = cp.makeClass(new ByteArrayInputStream(bytes));

				CtMethod[] declaredMethods = ct.getDeclaredMethods();
				for (CtMethod method : declaredMethods) {
					if (Modifier.isAbstract(method.getModifiers()))
						continue;

					String methodName = method.getName();
					if (config.getExcludes().contains(methodName)
							|| config.getExcludes().contains(method.getDeclaringClass().getSimpleName() + "." + methodName))
						continue;

					if (!config.isShowGetter() && method.getParameterTypes().length == 0
							&& (methodName.startsWith("get") || methodName.startsWith("is")))
						continue;

					countMap.putIfAbsent(methodName, new AtomicInteger());
					AtomicInteger counter = countMap.get(methodName);
					if (counter.getAndIncrement() > config.getMaxCount()) {
						System.out.println(methodName + ": count=" + counter.intValue());
						continue;
					}

					currentMethod = className + "." + methodName;

					String before = config.isShowEntry() ? "{ Stack.push(\"" + currentMethod + "\", $args);}" : "{Stack.push();}";

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

		return bytes;
	}
}
