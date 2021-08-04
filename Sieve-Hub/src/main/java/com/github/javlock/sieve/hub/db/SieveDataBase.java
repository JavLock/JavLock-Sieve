package com.github.javlock.sieve.hub.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.sieve.PdfFile;
import com.github.javlock.sieve.SieveTag;
import com.github.javlock.sieve.hub.SieveHub;
import com.github.javlock.sieve.hub.config.SieveHubDataBaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

import lombok.Getter;

public class SieveDataBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SieveDataBase.class.getSimpleName());
	private static final String HIDESTRING = "HIDE";

	private SieveHub hub;
	private JdbcPooledConnectionSource connectionSource;
	private @Getter Dao<PdfFile, String> pdfFileDAO;
	private @Getter Dao<SieveTag, String> sieveTagDAO;

	public SieveDataBase(SieveHub sieveHub) {
		this.hub = sieveHub;
	}

	private String createUrl(String host, int port, String dbName, String userName, String userPasswd, boolean ssl) {
		return "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + userPasswd
				+ "&ssl=" + ssl;
	}

	public void init() throws SQLException {
		SieveHubDataBaseConfig config = hub.getConfig().getDataBaseConfig();

		String host = config.getHost();
		int port = config.getPort();
		String dbName = config.getDbName();
		String userName = config.getUserName();
		String userPasswd = config.getUserPasswd();
		boolean ssl = config.isSsl();

		String databaseUrl = createUrl(host, port, dbName, userName, userPasswd, ssl);
		LOGGER.info("databaseUrl:{}", databaseUrl.replaceAll(userPasswd, HIDESTRING).replaceAll(userName, HIDESTRING));

		connectionSource = new JdbcPooledConnectionSource(databaseUrl);
		connectionSource.setMaxConnectionsFree(5);
		// connectionSource.setMaxConnectionsFree(60);
		connectionSource.initialize();

		initDAOs();

		TableUtils.createTableIfNotExists(connectionSource, PdfFile.class);
		TableUtils.createTableIfNotExists(connectionSource, SieveTag.class);
	}

	private void initDAOs() throws SQLException {
		this.pdfFileDAO = DaoManager.createDao(connectionSource, PdfFile.class);
		this.sieveTagDAO = DaoManager.createDao(connectionSource, SieveTag.class);
	}

	public void save(Object object) throws SQLException {

		if (object instanceof SieveTag) {
			SieveTag tag = (SieveTag) object;
			if (!sieveTagDAO.idExists(tag.getTag())) {
				sieveTagDAO.create(tag);
			} else {
				ArrayList<String> tagFiles = tag.getPdfFilesIds();
				SieveTag dbTag = sieveTagDAO.queryForId(tag.getTag());
				ArrayList<String> dbTagFiles = dbTag.getPdfFilesIds();
				for (String fileId : tagFiles) {
					if (!dbTagFiles.contains(fileId)) {
						dbTagFiles.add(fileId);
					}
				}
				sieveTagDAO.update(dbTag);
			}
			return;
		}
		if (object instanceof PdfFile) {
			PdfFile pdfFile = (PdfFile) object;
			if (!pdfFileDAO.idExists(pdfFile.getId())) {
				pdfFileDAO.create(pdfFile);
				LOGGER.info("{} saved", pdfFile);
			} else {
				PdfFile dbPdfFile = pdfFileDAO.queryForId(pdfFile.getId());
				if (dbPdfFile.isParsed()) {
					pdfFile.setParsed(dbPdfFile.isParsed());
				}
				pdfFileDAO.update(pdfFile);
			}
			return;
		}
		LOGGER.info("class {}", object.getClass());
	}

}
