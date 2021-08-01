package com.github.javlock.sieve.hub.network;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.sieve.hub.SieveHub;

import aaa.PdfFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandlerNetty extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandlerNetty.class.getSimpleName());

	private SieveHub hub;

	public ClientHandlerNetty(SieveHub thisHub) {
		this.hub = thisHub;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketAddress addr = ctx.channel().remoteAddress();
		Thread.currentThread().setName("Client:" + addr);
		LOGGER.info("connected");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("disconnected");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof PdfFile) {
			PdfFile pdfFile = (PdfFile) msg;
			hub.pdfWorker.append(pdfFile);
			return;
		}

		LOGGER.info("msg readed:{}", msg);

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("error:", cause);
	}

}
