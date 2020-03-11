package com.zeroturnaround.callspy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.Set;

import javassist.*;

public class CallSpy implements ClassFileTransformer {

    private Set<String> includes;
    private Set<String> excludes;
    private Set<String> excludeMethod;

    private boolean showEntry;

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

        String value = properties.getProperty("showEntry");
        showEntry = Boolean.valueOf(value);
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
                        if (Modifier.isAbstract(method.getModifiers()))
                            continue;

                        String name = method.getName();

                        method.getReturnType();

                        if (excludeMethod.contains(name))
                            continue;

                        if (method.getParameterTypes().length == 0 && (name.startsWith("get") || name.startsWith("is")))
                            continue;

                        String before = showEntry ? "{ Stack.push(\"" + className + "." + name + "\", $args);}" : "{Stack.push();}";

                        method.insertBefore(before);

                        String end = "{ Stack.log(\"" + className + "." + name + "\", $args, $type == void.class? \"void\": String.valueOf($_));}";

                        method.insertAfter(end);

                        method.insertAfter("{ Stack.pop(); }", true);
                    }

                    return ct.toBytecode();
                } catch (CannotCompileException e) {
                    e.printStackTrace();
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
