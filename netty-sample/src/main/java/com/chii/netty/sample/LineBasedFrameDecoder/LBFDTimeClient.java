package com.chii.netty.sample.LineBasedFrameDecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/3
 */
public class LBFDTimeClient {
	public static void main(String[] args) throws InterruptedException {
		int port = 8093;
		String host = "localhost";
		new LBFDTimeClient().connect(port, host);
	}

	public void connect(int port, String host) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
							socketChannel.pipeline().addLast(new StringDecoder());
							socketChannel.pipeline().addLast(new LBFDTimeClientHandler());
						}
					});

			ChannelFuture future = boot.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}

	}

	private class LBFDTimeClientHandler extends ChannelInboundHandlerAdapter {
		byte [] req;
		private int counter = 0;
		public LBFDTimeClientHandler() {
			req = ("QUERY TIMESTAMP" + System.getProperty("line.separator")).getBytes();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ByteBuf message;
			for (int i = 0; i < 100; i++) {
				message = Unpooled.buffer(req.length);
				message.writeBytes(req);
				ctx.writeAndFlush(message);
			}
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			String body = (String) msg;
			System.out.println("Now is : " + body + " . the counter is : " + ++counter);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
}
