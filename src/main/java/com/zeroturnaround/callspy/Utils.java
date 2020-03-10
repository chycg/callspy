package com.zeroturnaround.callspy;

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
				String s = arg instanceof String ? "\"" : "";
				sb.append(s).append(arg).append(s).append(",");
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

}
