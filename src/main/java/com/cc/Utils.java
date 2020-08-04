package com.cc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Utils {

	/**
	 * 是否显示入参/返参类型而非取值
	 */
	static boolean showParamType;

	public static String toString(String[] args) {
		return toString((Object[]) args);
	}

	public static String toString(Object[] args) {
		if (args == null || args.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				String text;
				String prefix = "";
				if (arg == null) {
					text = null;
				} else {
					prefix = arg instanceof String ? "\"" : "";
					text = toString(arg);
				}

				if (arg != null && arg.getClass().isArray()) {
					Class<?> elementType = arg.getClass().getComponentType();
					text = elementType.getName() + "[]";
				}

				sb.append(prefix).append(text).append(prefix).append(",");
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
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
					prefix = arg instanceof String ? "\"" : "";
					text = "<" + arg.getClass().getSimpleName() + ">";
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

	public static String toString(Object v) {
		if (v == null)
			return null;

		if (v.getClass().getName().startsWith("java"))
			return v.toString();

		return showParamType ? "<" + v.getClass().getSimpleName() + ">" : v.toString();
	}

	public static boolean isContain(Object o, Object... values) {
		if (o == null || isEmpty(values))
			return false;

		try {
			return Stream.of(values).anyMatch(e -> o.equals(e));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(o + ", values.size = " + values.length + ", v0 = " + values[0]);
		} catch (Error ex) {
			ex.printStackTrace();
			System.out.println(o + ", values.size = " + values.length + ", v0 = " + values[0]);
		}

		return false;
	}
}
