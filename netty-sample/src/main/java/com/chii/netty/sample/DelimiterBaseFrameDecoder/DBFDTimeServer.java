package com.chii.netty.sample.DelimiterBaseFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/3
 */
public class DBFDTimeServer {
	public static void main(String[] args) throws InterruptedException {
		int port = 8094;
		new DBFDTimeServer().bind(port);
	}

	public void bind(int port) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							//以$_为分隔符
							ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
							socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
							socketChannel.pipeline().addLast(new StringDecoder());
							socketChannel.pipeline().addLast(new DBFDTimeServerHandler());
						}
					});

			ChannelFuture future = boot.bind(port).sync();
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private class DBFDTimeServerHandler extends ChannelInboundHandlerAdapter {
		private int counter;

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			String body = (String) msg;
			System.out.println("receive order : " + body + ". the counter is : " + ++counter);
			body += "$_";
			ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
			ctx.writeAndFlush(echo);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
}
