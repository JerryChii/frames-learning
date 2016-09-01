package com.chii.bio.sample.TimeServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/8/29
 */
public class TimeServer {
	public static void main(String[] args) throws IOException {
		int port = 8888;
		//ServerSocket负责绑定IP地址，启动监听端口
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			//负责发起连接操作
			Socket socket = null;
			//一个链路一个线程处理
			while (true) {
				System.out.println("wait a new client connect");
				//当没有客户端连接的时候，会阻塞在这里
				socket = server.accept();
				new Thread(new TimeServerHandler(socket)).start();
			}
		} finally {
			if (server != null) {
				server.close();
				server = null;
			}
		}
	}
}
