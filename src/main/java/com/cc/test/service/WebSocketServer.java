package com.cc.test.service;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.cc.test.util.ArrayUtils;

@ServerEndpoint("/socket")
@Component
public class WebSocketServer {

	private static Session session;

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session) {
		WebSocketServer.session = session;
		sendMessage("websocket连接成功!");
	}

	@OnClose
	public void onClose() {
		sendMessage("websocket连接断开!");
	}

	@OnError
	public void onError(Session session, Throwable error) {
		error.printStackTrace();
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		if (ArrayUtils.isEmpty(message))
			return;
	}

	public void sendMessage(String message) {
		if (session == null)
			return;

		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
