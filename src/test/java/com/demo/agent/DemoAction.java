package com.demo.agent;

import java.io.IOException;

public class DemoAction {

    public static void main(String[] args) throws IOException {
//        One a = new One();
//        a.fun1(1);
//        a.fun2(2, 3);

        char c = (char) System.in.read();
        while (c != 'q') {
            int v = 30;
            System.out.println(v);

            c = (char) System.in.read();
        }
    }
}