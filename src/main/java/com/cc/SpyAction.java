package com.cc;

public class SpyAction {

	public static int fun1(int a) {
		System.out.println("this is fun 1.");
		fun12(a, a + 1);
		fun13(3333);

		return 1;
	}

	public static int fun12(int a, int b) {
		System.out.println("this is fun 12.");
		return 12;
	}

	public static int fun13(int v) {
		System.out.println("this is fun 13.");
		return 13;
	}

	public static int fun2(int a, int b) {
		System.out.println("this is fun 2.");

		System.out.println(new User("cc", 18).sayHello("hehe", 5));
		return 2;
	}

	public static void main(String[] args) {
		fun1(1);
		fun2(2, 3);
	}

}