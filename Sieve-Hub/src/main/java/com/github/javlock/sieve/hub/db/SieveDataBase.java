package com.github.javlock.sieve.hub.db;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.sieve.hub.SieveHub;
import com.github.javlock.sieve.hub.config.SieveHubDataBaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import aaa.PdfFile;

public class SieveDataBase {
	private SieveHub hub;
	private static final Logger LOGGER = LoggerFactory.getLogger(SieveDataBase.class.getSimpleName());

	public SieveDataBase(SieveHub sieveHub) {
		this.hub = sieveHub;
	}

	private String databaseUrl = "";
	private ConnectionSource connectionSource;

	Dao<PdfFile, String> pdfFileDao;

	public void init() throws SQLException {
		SieveHubDataBaseConfig config = hub.getConfig().getDataBaseConfig();

		String host = config.getHost();
		int port = config.getPort();
		String dbName = config.getDbName();
		String userName = config.getUserName();
		String userPasswd = config.getUserPasswd();
		boolean ssl = config.isSsl();

		databaseUrl = createUrl(host, port, dbName, userName, userPasswd, ssl);
		LOGGER.info("databaseUrl:{}", databaseUrl.replaceAll(userPasswd, "HIDE"));

		connectionSource = new JdbcConnectionSource(databaseUrl);

		pdfFileDao = DaoManager.createDao(connectionSource, PdfFile.class);
		TableUtils.createTableIfNotExists(connectionSource, PdfFile.class);
	}

	private String createUrl(String host, int port, String dbName, String userName, String userPasswd, boolean ssl) {
		return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + userPasswd
				+ "&ssl=" + ssl;
	}

}
