package com.chii.netty.sample.DelimiterBaseFrameDecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/3
 */
public class DBFDTimeClient {
	public static void main(String[] args) throws InterruptedException {
		int port = 8094;
		String host = "localhost";
		new DBFDTimeClient().connect(port, host);
	}

	public void connect(int port, String host) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					//.handler(new LoggingHandler(LogLevel.INFO))
					.handler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
							socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
							socketChannel.pipeline().addLast(new StringDecoder());
							socketChannel.pipeline().addLast(new DBFDTimeClientHandler());
						}
					});

			ChannelFuture future = boot.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}

	}

	private class DBFDTimeClientHandler extends ChannelInboundHandlerAdapter {
		String req = "what the fuck.$_";
		private int counter = 0;
		public DBFDTimeClientHandler() {
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			for (int i = 0; i < 100; i++) {
				ctx.writeAndFlush(Unpooled.copiedBuffer(req.getBytes()));
			}
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("Now is : " + msg + " . the counter is : " + ++counter);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
}
