package com.chii.netty.sample.HttpServe;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/4
 */
public class HttpFileServer {
	private static final String DEFAULT_URL = "/netty-sample/src/main/java/com/chii";

	public static void main(String[] args) throws InterruptedException {
		int port = 8096;
		String url = DEFAULT_URL;
		new HttpFileServer().run(port, "localhost", url);
	}

	public void run(final int port, final String host, final String url) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
							socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
							socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
							socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
							socketChannel.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url));
						}
					});

			ChannelFuture future = boot.bind(host, port).sync();
			System.out.println("http file server start.");
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}


	private class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
		private final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
		private final Pattern ALLOWED_FILE_NAME = Pattern
				.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

		private final String url;

		public HttpFileServerHandler(String url) {
			this.url = url;
		}

		protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
			if (!request.getDecoderResult().isSuccess()) {
				sendError(ctx, BAD_REQUEST);
				return;
			}
			if (request.getMethod() != GET) {
				sendError(ctx, METHOD_NOT_ALLOWED);
				return;
			}
			final String uri = request.getUri();
			final String path = sanitizeUri(uri);
			if (path == null) {
				sendError(ctx, FORBIDDEN);
				return;
			}
			File file = new File(path);
			if (file.isHidden() || !file.exists()) {
				sendError(ctx, NOT_FOUND);
				return;
			}
			if (file.isDirectory()) {
				if (uri.endsWith("/")) {
					sendListing(ctx, file);
				} else {
					sendRedirect(ctx, uri + '/');
				}
				return;
			}
			if (!file.isFile()) {
				sendError(ctx, FORBIDDEN);
				return;
			}

			RandomAccessFile randomAccessFile = null;

			try {
				randomAccessFile = new RandomAccessFile(file, "r");
			} catch (FileNotFoundException e) {
				sendError(ctx, NOT_FOUND);
				return;
			}

			long fileLength = randomAccessFile.length();

			HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
			setContentLength(response, fileLength);
			setContentTypeHeader(response, file);
			if (isKeepAlive(request)) {
				response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			ctx.write(response);
			ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());

			sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
				public void operationProgressed(ChannelProgressiveFuture future,
												long progress, long total) {
					if (total < 0) { // total unknown
						System.err.println("Transfer progress: " + progress);
					} else {
						System.err.println("Transfer progress: " + progress + " / "
								+ total);
					}
				}

				public void operationComplete(ChannelProgressiveFuture future)
						throws Exception {
					System.out.println("Transfer complete.");
				}
			});
			ChannelFuture lastContentFuture = ctx
					.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			if (!isKeepAlive(request)) {
				lastContentFuture.addListener(ChannelFutureListener.CLOSE);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			if (ctx.channel().isActive()) {
				sendError(ctx, INTERNAL_SERVER_ERROR);
			}
		}

		private String sanitizeUri(String uri) {
			try {
				uri = URLDecoder.decode(uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				try {
					uri = URLDecoder.decode(uri, "ISO-8859-1");
				} catch (UnsupportedEncodingException e1) {
					throw new Error();
				}
			}
			if (!uri.startsWith(url)) {
				return null;
			}
			if (!uri.startsWith("/")) {
				return null;
			}
			uri = uri.replace('/', File.separatorChar);
			if (uri.contains(File.separator + '.')
					|| uri.contains('.' + File.separator) || uri.startsWith(".")
					|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
				return null;
			}
			return System.getProperty("user.dir") + File.separator + uri;
		}

		private void sendListing(ChannelHandlerContext ctx, File dir) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
			response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			StringBuilder buf = new StringBuilder();
			String dirPath = dir.getPath();
			buf.append("<!DOCTYPE html>\r\n");
			buf.append("<html><head><title>");
			buf.append(dirPath);
			buf.append(" 目录：");
			buf.append("</title></head><body>\r\n");
			buf.append("<h3>");
			buf.append(dirPath).append(" 目录：");
			buf.append("</h3>\r\n");
			buf.append("<ul>");
			buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
			for (File f : dir.listFiles()) {
				if (f.isHidden() || !f.canRead()) {
					continue;
				}
				String name = f.getName();
				if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
					continue;
				}
				buf.append("<li>链接：<a href=\"");
				buf.append(name);
				buf.append("\">");
				buf.append(name);
				buf.append("</a></li>\r\n");
			}
			buf.append("</ul></body></html>\r\n");
			ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
			response.content().writeBytes(buffer);
			buffer.release();
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
			response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
			response.headers().set(LOCATION, newUri);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		private void setContentTypeHeader(HttpResponse response, File file) {
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
		}
	}
}
