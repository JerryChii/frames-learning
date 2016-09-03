package com.chii.netty.sample.stickpackage;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/3
 */
public class StickTimeClient {
	public static void main(String[] args) throws InterruptedException {
		int port = 8092;
		new StickTimeClient().bind(port, "localhost");

	}

	public void bind(int port, String host) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new StickClientHandler());
						}
					});
			ChannelFuture future = boot.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}

	}

	private class StickClientHandler extends ChannelInboundHandlerAdapter {

		byte [] req;
		private int counter = 0;
		public StickClientHandler() {
//			req = ("QUERY TIMESTAMP" + System.getProperty("line.separator")).getBytes();
			req = ("QUERY TIMESTAMP").getBytes();
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
			ByteBuf buf = (ByteBuf) msg;
			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			System.out.println("Now is : " + new String (req, "utf-8") + " . the counter is : " + ++counter);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
}
