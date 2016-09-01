package com.chii.netty.sample.TimeServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/1
 */
public class TimeServer {
	public static void main(String[] args) {
		int port = 8091;
		try {
			new TimeServer().bind(port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void bind(int port) throws InterruptedException {
		//reactor线程组，一个用于服务端接收客户端连接，另一个用于进行SocketChannel的网络读写
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChannelInitializer<SocketChannel> () {

						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new DiscardServerHandler ());
						}
					});
			ChannelFuture future = boot.bind(port).sync();

			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
