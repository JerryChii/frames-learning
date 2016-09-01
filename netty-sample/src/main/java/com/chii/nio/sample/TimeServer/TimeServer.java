package com.chii.nio.sample.TimeServer;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/8/29
 */
public class TimeServer {
	public static void main(String[] args) {
		int port = 8890;
		MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
		new Thread(timeServer, "NIO-MultiplexerTimeServer-001").start();
	}
}
