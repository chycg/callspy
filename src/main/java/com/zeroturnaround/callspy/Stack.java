package com.zeroturnaround.callspy;

public class Stack {

    static String indent = "";

    public static void push() {
        indent += " ";
    }

    public static void pop() {
        if (indent.isEmpty())
            return;

        indent = indent.substring(1);
    }

    public static void push(String method, Object[] args) {
        push();

        log(method, args);
    }

    public static void log(String string) {
        System.out.println(indent + string);
    }

    public static void log(String method, Object[] args) {
        log(method + "(" + Utils.toString(args) + ")");
    }

    public static void log(String method, Object[] args, Object returnValue) {
        log(method + "(" + Utils.toString(args) + ") -> " + returnValue);
    }
}
