package com.cc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {

	public static String toString(String[] args) {
		return toString((Object[]) args);
	}

	public static String toString(Object[] args) {
		if (args == null || args.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				String prefix = arg instanceof String ? "\"" : "";

				String text = arg == null ? null : arg.toString();
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

}
