package com.chii.bio.sample;

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
public class TimeClient {
	public static void main(String[] args) throws IOException {
		//int port = 8888;
		int port = 8889;
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			socket = new Socket("localhost", port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("QUERY TIMESTAMP");
			out.println("AAA");
			String resp = in.readLine();
			System.out.println("Now Timestamp is : " + resp);
			resp = in.readLine();
			System.out.println("Now Timestamp is : " + resp);
		} catch (IOException e) {
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
			if (in != null) {
				in.close();
				in = null;
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
	}
}
