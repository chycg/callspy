package com.cc.tree;

import java.awt.Color;
import java.util.stream.Stream;

public enum Mod {

	DEFAULT(0, ' ', Color.magenta),

	PUBLIC(1, '+', Color.blue),

	PRIVATE(2, '-', Color.darkGray),

	PROTECTED(4, '~', Color.cyan.darker());

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

}
