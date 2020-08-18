package com.demo.dubbo.provider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Created by Sean on 2015-12-14.
 */
public class HelloServiceImpl implements HelloService {
    /**
     * Say Hello.
     *
     * @param name 姓名
     * @return 问好内容
     */
    public String sayHello(String name) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [Receive] " + name + ", from consumer: " + RpcContext.getContext().getRemoteAddress());
        String hello = "Hello " + name + ", response form provider: " + RpcContext.getContext().getLocalAddress();
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [Response] " + hello);
        return hello;
    }

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("/META-INF/spring/hello-provider.xml");
        System.in.read();
        classPathXmlApplicationContext.close();
	}
}
