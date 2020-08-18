package com.cc.tree;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Mod {

	PUBLIC(1, '+', Color.black),

	PROTECTED(2, '-', Color.green.darker()),

	DEFAULT(3, '~', Color.blue),

	PRIVATE(4, '*', Color.magenta);

	private final int code;

	private final char sign;

	private final Color color;

	private static Map<Integer, Mod> modMap;

	private Mod(int code, char sign, Color color) {
		this.code = code;
		this.sign = sign;
		this.color = color;
	}

	public int getCode() {
		return code;
	}

	public char getSign() {
		return sign;
	}

	public Color getColor() {
		return color;
	}

	public static Mod getModByChar(char c) {
		return Stream.of(Mod.values()).filter(e -> e.sign == c).findAny().orElse(Mod.PUBLIC);
	}

	public static Mod getModByCode(int code) {
		if (modMap == null) {
			modMap = Stream.of(Mod.values()).collect(Collectors.toMap(e -> e.getCode(), e -> e));
		}

		return modMap.getOrDefault(code, Mod.PUBLIC);
	}

	public static boolean isModSign(char c) {
		return Stream.of(Mod.values()).anyMatch(e -> e.sign == c);
	}

}
