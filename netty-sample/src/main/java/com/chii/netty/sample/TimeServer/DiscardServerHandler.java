package com.chii.netty.sample.TimeServer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/1
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

	/**
	 * Please keep in mind that it is the handler's responsibility to release any reference-counted object passed to the handler.
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			ByteBuf buf = (ByteBuf) msg;
			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			String body = new String(req, "UTF-8");
			System.out.println("request: " + body);
			String currentTime = "QUERY TIMESTAMP".equalsIgnoreCase(body) ? System.currentTimeMillis() + "" : "BAD ORDER";

			ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
			ctx.write(resp);
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		//将消息发送队列中的消息写入到SocketChannel中发送给对方
		ctx.flush();
	}

	/**
	 * the caught exception should be logged and its associated channel should be closed here
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}
