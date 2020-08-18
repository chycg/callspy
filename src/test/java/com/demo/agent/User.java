package com.demo.agent;

import java.time.LocalDate;

public class User {

	private String name;

	private int age;

	public User(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public int getBirthDay() {
		return LocalDate.now().getYear() - age;
	}

	public String sayHello(String greet, int time) {
		int value = getBirthDay();
		for (int i = 0; i < time; i++) {
			value = calc(value);
		}

		return "Hello," + name + "," + greet + ", your value is: " + value;
	}

	public int calc(int v) {
		return v * 2;
	}

}