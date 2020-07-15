package com.cc.test;

public class SpyAction {

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

    public static void fun2(int a, int b) {
        System.out.println(new User("cc", 18).sayHello("hehe", 5));
    }

    public static void main(String[] args) {
        fun1(1);
        fun2(2, 3);
    }

}