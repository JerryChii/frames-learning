package com.chii.bio.sample.TimeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/8/29
 */
public class TimeServerHandler implements Runnable {
	private Socket socket;

	public TimeServerHandler() {}

	public TimeServerHandler(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);
			String currentTime = null;
			String body = null;
			while (true) {
				body = in.readLine();
				if (body == null) break;
				currentTime = "QUERY TIMESTAMP".equalsIgnoreCase(body) ? System.currentTimeMillis() + "" : "BAD ORDER";
				out.println(currentTime);
			}
		} catch (IOException e) {
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (this.socket != null) {
				try {
					this.socket.close();
					this.socket = null;
				} catch (IOException e3) {
					e3.printStackTrace();
				}
			}
		}
	}
}
