package com.cc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.json.JSONObject;

public class Utils {

	/**
	 * 是否显示入参/返参类型而非取值
	 */
	static boolean showParamType = false;

	static boolean showJson = false;

	/**
	 * 简单类名
	 */
	static boolean showSimpleClzName = false;

	/**
	 * trace args
	 * 
	 * @param args
	 * @return
	 */
	public static String getArgs(Object[] args) {
		if (args == null || args.length == 0)
			return "";

		return toArrayString(args);
	}

	public static String toArrayString(Object[] args) {
		if (args == null)
			return null;

		if (args.length == 0)
			return args.getClass().getComponentType().getName() + "[]";

		StringBuilder sb = new StringBuilder();
		for (Object arg : args) {
			String text = toString(arg);
			String prefix = arg instanceof String ? "\"" : "";
			sb.append(prefix).append(text).append(prefix).append(",");
		}

		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static String toString(Object arg) {
		if (arg == null)
			return null;

		if (arg instanceof Class)
			return ((Class<?>) arg).getCanonicalName();

		Class<?> clz = arg.getClass();
		if (clz.getSimpleName().contains("$Proxy")) // proxy type
			return "$Proxy";

		if (clz.isArray())
			return getArrayString(arg);

		if (clz.getName().startsWith("java"))
			return getString(arg);

		if (showJson) {
			String text = clz.getSimpleName() + "=" + JSONObject.valueToString(arg);
			if (text.length() > 64)
				text = makeSimpleName(arg);

			return text;
		}

		return getString(arg);
	}

	private static String getString(Object arg) {
		String str = arg.toString();
		if (!showSimpleClzName || !str.contains("@"))
			return str;

		return makeSimpleName(arg);
	}

	/**
	 * 原始类型
	 * 
	 * @param arg
	 * @return
	 */
	private static String getArrayString(Object arg) {
		Class<?> clz = arg.getClass();
		String type = clz.getName();
		if (!type.startsWith("["))
			return toArrayString((Object[]) arg);

		switch (type) {
		case "[B":
			return Arrays.toString((byte[]) arg);
		case "[I":
			return Arrays.toString((int[]) arg);
		case "[S":
			return Arrays.toString((short[]) arg);
		case "[C":
			return Arrays.toString((char[]) arg);
		case "[Z":
			return Arrays.toString((boolean[]) arg);
		case "[D":
			return Arrays.toString((double[]) arg);
		case "[J":
			return Arrays.toString((long[]) arg);
		case "[F":
			return Arrays.toString((float[]) arg);

		default:
			break;
		}

		return arg.toString();
	}

	private static String makeSimpleName(Object arg) {
		return "【" + arg.getClass().getSimpleName() + "】";
	}

	public static String[] getArgTypes(Object[] args) {
		if (args == null || args.length == 0)
			return new String[0];

		StringBuilder sb = new StringBuilder();
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				String text;
				String prefix = "";
				if (arg == null) {
					text = null;
				} else {
					text = makeSimpleName(arg);
				}

				if (arg != null && arg.getClass().isArray()) {
					Class<?> elementType = arg.getClass().getComponentType();
					text = elementType.getName() + "[]";
				}

				sb.append(prefix).append(text).append(prefix).append(",");
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		return new String[] { sb.toString() };
	}

	public static String toString(Collection<?> c) {
		if (c == null || c.isEmpty())
			return "";

		String splitChar = ",";

		StringBuilder sb = new StringBuilder(c.size() * 8);
		for (Object o : c)
			sb.append(o).append(splitChar);

		if (sb.length() > 0)
			sb.delete(sb.lastIndexOf(splitChar), sb.length());

		return sb.toString();
	}

	public static Set<String> splitString(String s) {
		Set<String> data = new HashSet<>();

		if (s == null || s.trim().isEmpty())
			return data;

		for (String e : s.trim().split(",")) {
			e = e.trim();
			if (e.isEmpty())
				continue;

			data.add(e);
		}

		return data;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	public static <T> boolean isNotEmpty(T[] array) {
		return !isEmpty(array);
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> c) {
		return !isEmpty(c);
	}

	public static boolean isContain(Object o, Object... values) {
		if (o == null || isEmpty(values))
			return false;

		return Stream.of(values).anyMatch(e -> o.equals(e));
	}

	public static void main(String[] args) {
		Object b = new byte[] { 1, 2, 3 };
		byte[] l2 = (byte[]) b;
		System.out.println(Arrays.toString(l2));

	}
}
