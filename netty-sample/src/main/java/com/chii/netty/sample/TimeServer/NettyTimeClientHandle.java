package com.chii.netty.sample.TimeServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/1
 */
public class NettyTimeClientHandle extends ChannelInboundHandlerAdapter {
	private final ByteBuf firstMessage;

	public NettyTimeClientHandle() {
		byte [] req = "QUERY TIMESTAMP".getBytes();
		firstMessage = Unpooled.buffer(req.length);
		firstMessage.writeBytes(req);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, "UTF-8");
		System.out.println("Now is : " + body);
	}

	//建立链接成功后调用
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("-----");
		ctx.writeAndFlush(firstMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}
