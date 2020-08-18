package com.demo.dubbo.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Sean on 2015-12-15.
 */
public class Consumer {

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/spring/hello-consumer.xml");

		HelloService priHelloService = (HelloService) ctx.getBean("helloService", HelloService.class);

		int c = 'a';
		while (c != 'q') {
			if (c == 13 || c == 10) {
				c = System.in.read();
				continue;
			}

			String name = "Sean " + (char) c;
			System.err.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [Request] " + name);
			String hello = priHelloService.sayHello(name);
			System.err.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [Receive] " + hello);

			c = System.in.read();
		}
	}
}
