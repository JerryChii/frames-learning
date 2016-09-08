package com.chii.netty.sample.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;

import java.util.List;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/8
 */
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {

	MarshallingEncoder marshallingEncoder;

	public NettyMessageEncoder() throws IOException {
		this.marshallingEncoder = new MarshallingEncoder();
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, List<Object> list) throws Exception {

	}
}
