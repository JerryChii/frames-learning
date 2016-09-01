package com.chii.netty.sample.TimeServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/1
 */
public class TimeClient {
	public static void main(String[] args) throws InterruptedException {
		int port = 8091;
		new TimeClient().connect(port, "localhost");
	}

	public void connect(int port, String host) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new NettyTimeClientHandle());
						}
					});

			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} finally {
			//优雅退出，释放NIO线程组资源
			group.shutdownGracefully();
		}

	}
}
