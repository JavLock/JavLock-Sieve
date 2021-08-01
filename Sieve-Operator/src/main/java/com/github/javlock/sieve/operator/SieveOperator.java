package com.github.javlock.sieve.operator;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.sieve.operator.config.SieveOperatorConfig;
import com.github.javlock.sieve.operator.gui.OperatorGuiMain;
import com.github.javlock.sieve.operator.network.ClientHandlerNetty;

import aaa.Event;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.Setter;

public class SieveOperator extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(SieveOperator.class.getSimpleName());

	private @Getter @Setter File dataDir;
	private @Getter @Setter File configFile;

	private @Getter @Setter SieveOperatorConfig config = new SieveOperatorConfig();
	private OperatorGuiMain operatorGuiMain = new OperatorGuiMain();
	private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
	private EventLoopGroup group = new NioEventLoopGroup();
	private Bootstrap clientBootstrap = new Bootstrap();
	private @Getter ChannelFuture channelFuture;

	public int currentLevel;

	public void init() throws ConfigurationException, IOException {
		initConfig();
		initNetwork();
		initGui();
	}

	private void initGui() {
		operatorGuiMain.setOperator(this);
	}

	private void initConfig() throws IOException, ConfigurationException {
		if (!configFile.exists()) {
			if (configFile.getParentFile().mkdirs() || configFile.createNewFile()) {
				objectMapper.writeValue(configFile, config);
			}
			throw new ConfigurationException("edit config file " + configFile.getAbsolutePath());
		} else {
			setConfig(objectMapper.readValue(configFile, SieveOperatorConfig.class));
		}
	}

	private void initNetwork() {
		LOGGER.info("initNetwork-start");
		clientBootstrap.group(group);
		clientBootstrap.channel(NioSocketChannel.class);
		clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ObjectEncoder());
				ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,
						ClassResolvers.softCachingConcurrentResolver(Event.class.getClassLoader())));
				ch.pipeline().addLast(new ClientHandlerNetty());
			}
		});
		LOGGER.info("initNetwork-end");
	}

	@Override
	public void run() {
		try {
			startNetwork();
			operatorGuiMain.setVisible(true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startNetwork() throws InterruptedException {
		LOGGER.info("startNetwork-start");
		String host = getConfig().getHost();
		int port = getConfig().getPort();
		clientBootstrap.remoteAddress(new InetSocketAddress(host, port));
		channelFuture = clientBootstrap.connect().sync();
		LOGGER.info("startNetwork-end");

	}
}
