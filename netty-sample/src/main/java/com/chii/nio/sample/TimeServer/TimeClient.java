package com.chii.nio.sample.TimeServer;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/8/30
 */
public class TimeClient {
	public static void main(String[] args) {
		int port = 8890;
		new Thread(new TimeClientHandle("localhost", port)).start();
	}
}
