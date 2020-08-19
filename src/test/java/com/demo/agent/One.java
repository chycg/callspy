package com.demo.agent;

public class One {

	public int fun1(int a) {
		a = 1000;
		Other o = new Other();
		o.fun12(a, a + 1);
		o.fun13(3333);

		return 1;
	}

	private static int getTime(int t) {
		if (t >= 5)
			return t;

		return getTime(t + 1);
	}

	public void fun2(int a, int b) {
		User user = new User("cc", 18);
		int time = getTime(0);
		String sayHello = user.sayHello("hehe", time);
		System.out.println(sayHello);
	}

}
