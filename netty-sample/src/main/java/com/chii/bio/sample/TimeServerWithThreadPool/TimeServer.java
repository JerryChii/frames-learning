package com.chii.bio.sample.TimeServerWithThreadPool;

import com.chii.bio.sample.TimeServer.TimeServerHandler;

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
		int port = 8889;
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			Socket socket = server.accept();
			TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50, 10000);
			singleExecutor.execute(new TimeServerHandler(socket));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				server.close();
				server = null;
			}
		}

	}
}
