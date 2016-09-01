package com.chii.nio.sample.TimeServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/8/30
 */
public class TimeClientHandle implements Runnable {
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;

	public TimeClientHandle(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				SelectionKey key = null;
				while (iterator.hasNext()) {
					key = iterator.next();
					iterator.remove();
					handleInput(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			//判断是否连接成功
			SocketChannel sc = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				}
			} else {
				System.exit(1);
			}
			if (key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now Timestamp : " + body);
					this.stop = true;
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				} else ;
			}
		}
	}

	private void doWrite(SocketChannel sc) throws IOException {
		byte[] req = "QUERY TIMESTAMP".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		sc.write(writeBuffer);
		if (! writeBuffer.hasRemaining()) {
			System.out.println("Send order 2 server succeed");
		}
	}

	private void doConnect() throws IOException {
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
		} else {
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}
}
