package com.github.javlock.sieve.hub;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.sieve.hub.config.SieveHubConfig;
import com.github.javlock.sieve.hub.db.SieveDataBase;
import com.github.javlock.sieve.hub.network.ClientHandlerNetty;
import com.github.javlock.sieve.hub.worker.PdfWorker;

import aaa.Event;
import aaa.Test;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.Setter;

public class SieveHub extends Thread {
	private @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
	private @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();
	private @Getter ChannelFuture channelFuture;
	private SieveHub thisHub = this;
	private @Getter SieveDataBase db = new SieveDataBase(this);

	private static final Logger LOGGER = LoggerFactory.getLogger(SieveHub.class.getSimpleName());

	private @Getter @Setter File dataDir;
	private @Getter @Setter File configFile;
	private @Getter @Setter SieveHubConfig config = new SieveHubConfig();

	private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	public PdfWorker pdfWorker = new PdfWorker(this);

	public void init() throws IOException, ConfigurationException, SQLException {
		initConfig();
		initDB();
		initNetwork();
	}

	private void initDB() throws SQLException {
		db.init();
	}

	private void initConfig() throws IOException, ConfigurationException {
		if (!configFile.exists()) {
			if (configFile.getParentFile().mkdirs() || configFile.createNewFile()) {
				objectMapper.writeValue(configFile, config);
			}
			throw new ConfigurationException("edit config file " + configFile.getAbsolutePath());
		} else {
			setConfig(objectMapper.readValue(configFile, SieveHubConfig.class));
		}
	}

	private void initNetwork() {
		LOGGER.info("initNetwork-start");
		serverBootstrap.group(serverWorkgroup).channel(NioServerSocketChannel.class)
				.localAddress(new InetSocketAddress(config.getNetworkConfig().getPort()));
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,
						ClassResolvers.softCachingConcurrentResolver(Event.class.getClassLoader())));
				ch.pipeline().addLast(new ObjectEncoder());
				ch.pipeline().addLast(new ClientHandlerNetty(thisHub));
				ch.writeAndFlush(new Test("111", 123));
			}
		});
		LOGGER.info("initNetwork-end");
	}

	@Override
	public void run() {
		pdfWorker.start();
		startNetwork();

	}

	private void startNetwork() {
		this.channelFuture = serverBootstrap.bind();
	}

}
