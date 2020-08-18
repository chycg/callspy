package com.cc.tree;

import java.awt.Color;
import java.util.stream.Stream;

public enum Mod {

	PUBLIC(1, '+', Color.black),

	PROTECTED(2, '-', Color.green.darker()),

	DEFAULT(3, '~', Color.blue),

	PRIVATE(4, '*', Color.magenta);

	private final int code;

	private final char sign;

	private final Color color;

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

	public static Mod getModByChar(char c) {
		return Stream.of(Mod.values()).filter(e -> e.sign == c).findAny().orElse(Mod.PUBLIC);
	}

	public static Mod getModByCode(int code) {
		return Stream.of(Mod.values()).filter(e -> e.code == code).findAny().orElse(Mod.PUBLIC);
	}

	public Color getColor() {
		return color;
	}

	public static boolean isModSign(char c) {
		return Stream.of(Mod.values()).anyMatch(e -> e.sign == c);
	}

}
