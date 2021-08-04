package com.github.javlock.sieve.hub.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.sieve.PdfFile;
import com.github.javlock.sieve.SieveTag;
import com.github.javlock.sieve.hub.SieveHub;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import lombok.NonNull;

public class PdfWorker extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfWorker.class.getSimpleName());
	private SieveHub hub;

	long maxQueue = 100;
	long maxTasks = 50;
	final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);

	private ConcurrentHashMap<String, PdfFile> pdfFileMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Thread> tasks = new ConcurrentHashMap<>();

	public PdfWorker(SieveHub sieveHub) {
		this.hub = sieveHub;
	}

	private Runnable appendFromDB() {
		return () -> {
			Thread.currentThread().setName("PdfWorker-appender");
			if (pdfFileMap.size() >= maxQueue) {
				return;
			}

			try {
				QueryBuilder<PdfFile, String> queryBuilder = hub.getDb().getPdfFileDAO().queryBuilder();

				Where<PdfFile, String> where = queryBuilder.where();
				where.eq("parsed", false);

				queryBuilder.limit(maxQueue - pdfFileMap.size());

				PreparedQuery<PdfFile> p = queryBuilder.prepare();
				List<PdfFile> pdfFiles = hub.getDb().getPdfFileDAO().query(p);

				for (PdfFile pdfFile : pdfFiles) {
					if (pdfFile.getData().length == 0) {
						hub.getDb().getPdfFileDAO().deleteById(pdfFile.getId());
					} else {
						pdfFileMap.putIfAbsent(pdfFile.getId(), pdfFile);
					}
				}
			} catch (SecurityException | SQLException e1) {
				e1.printStackTrace();
			}
		};
	}

	private ArrayList<SieveTag> createTagsByText(@NonNull PdfFile pdfFile, @NonNull String text) {
		ArrayList<SieveTag> tags = new ArrayList<>();
		String[] ar = text.split(" ");

		for (String string : ar) {
			string = string.replaceAll("\n", "");
			if (string.isEmpty()) {
				continue;
			}

			SieveTag tag = new SieveTag();
			tag.setTag(string);
			if (!tags.contains(tag)) {
				tags.add(tag);
			}
		}
		return tags;
	}

	@Override
	public void run() {
		service.scheduleWithFixedDelay(appendFromDB(), 0, 10, TimeUnit.SECONDS);
		scheduler.scheduleWithFixedDelay(work(), 0, 3, TimeUnit.SECONDS);
	}

	private Runnable work() {
		return () -> {
			Thread.currentThread().setName("PdfWorker-parser");
			if (pdfFileMap.isEmpty()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;
			}
			//
			for (Map.Entry<String, PdfFile> pEntry : pdfFileMap.entrySet()) {
				String hash = pEntry.getKey();
				PdfFile pdfFile = pEntry.getValue();

				if (tasks.size() >= maxTasks) {
					continue;
				}

				// NEW TASK
				Thread thread = new Thread((Runnable) () -> {
					//
					byte[] data = pdfFile.getData();
					try {
						try (PDDocument doc = PDDocument.load(data)) {
							String text = new PDFTextStripper().getText(doc);
							ArrayList<SieveTag> tags = createTagsByText(pdfFile, text);
							for (SieveTag sieveTag : tags) {
								if (!sieveTag.getPdfFilesIds().contains(pdfFile.getId())) {
									sieveTag.getPdfFilesIds().add(pdfFile.getId());
								}
								hub.getDb().save(sieveTag);
							}
						}
						pdfFile.setParsed(true);
						hub.getDb().save(pdfFile);
					} catch (Exception e2) {
						if (e2.getMessage().hashCode() == -532426698) {
							try {
								File errorDir = hub.getErrorDataDir();
								if (!errorDir.exists()) {
									errorDir.mkdirs();
								}
								File file = new File(errorDir, hash);
								if (!file.exists()) {
									file.createNewFile();
								}
								Files.write(file.toPath(), data, StandardOpenOption.TRUNCATE_EXISTING);
							} catch (IOException e) {
								e.printStackTrace();
							}
							pdfFileMap.remove(pdfFile.getId());
							tasks.remove(pdfFile.getId());
						} else {
							LOGGER.error("error2 ", e2);
						}
						try {
							hub.getDb().getPdfFileDAO().deleteById(hash);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					pdfFileMap.remove(pdfFile.getId());
					tasks.remove(pdfFile.getId());
				}, "PdfWorker-parser-" + hash.substring(0, 20));
				if (!tasks.containsKey(pdfFile.getId())) {
					tasks.put(pdfFile.getId(), thread);
					thread.start();
				}
			}
		};
	}

}
