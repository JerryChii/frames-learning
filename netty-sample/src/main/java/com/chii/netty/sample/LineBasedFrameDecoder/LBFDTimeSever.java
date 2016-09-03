package com.chii.netty.sample.LineBasedFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/3
 */
public class LBFDTimeSever {
	public static void main(String[] args) throws InterruptedException {
		int port = 8094;
		new LBFDTimeSever().bind(port);
	}

	public void bind(int port) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							//换行符为标志的解码器，如果字节数达到1024还没有出现换行符就会抛出异常
							socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
							//将受到的对象转化成字符串，要写在Handler的前面
							socketChannel.pipeline().addLast(new StringDecoder());
							socketChannel.pipeline().addLast(new LBFDTimeSeverHandler());
						}
					});


			ChannelFuture future = boot.bind(port).sync();
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}


	}

	private class LBFDTimeSeverHandler extends ChannelInboundHandlerAdapter {
		private int counter;

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			String body = (String) msg;
			System.out.println("receive order : " + body + ". the counter is : " + ++counter);
			String currentTime = "QUERY TIMESTAMP".equalsIgnoreCase(body) ? System.currentTimeMillis() + "" : "BAD ORDER";
			currentTime += System.getProperty("line.separator");
			ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
			ctx.writeAndFlush(resp);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
}
