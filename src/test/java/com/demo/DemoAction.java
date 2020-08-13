package com.demo;

public class DemoAction {

	public static int fun1(int a) {
		a = 1000;
		fun12(a, a + 1);
		fun13(3333);

		return 1;
	}

	public static int fun12(int a, int b) {
		return 12;
	}

	public static int fun13(int v) {
		return 13;
	}

	private static int getTime(int t) {
		if (t >= 5)
			return t;

		return getTime(t + 1);
	}

	public static void fun2(int a, int b) {
		User user = new User("cc", 18);
		int time = getTime(0);
		String sayHello = user.sayHello("hehe", time);
		System.out.println(sayHello);
	}

	public static void main(String[] args) {
		fun1(1);
		fun2(2, 3);
	}

}