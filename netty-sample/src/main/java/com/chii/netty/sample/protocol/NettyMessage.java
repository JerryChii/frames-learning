package com.chii.netty.sample.protocol;

import lombok.Data;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/8
 */
@Data
public class NettyMessage {
	private Header header;
	private Object body;

	@Override
	public String toString() {
		return "NettyMessage [header=" + header + "]";
	}
}
