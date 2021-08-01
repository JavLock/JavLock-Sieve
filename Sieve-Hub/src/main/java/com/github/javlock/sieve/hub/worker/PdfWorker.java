package com.github.javlock.sieve.hub.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.sieve.hub.SieveHub;

import aaa.PdfFile;
import aaa.SieveTag;
import lombok.NonNull;

public class PdfWorker extends Thread {

	private SieveHub hub;
	private ConcurrentHashMap<String, PdfFile> pdfFileMap = new ConcurrentHashMap<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfWorker.class.getSimpleName());

	public PdfWorker(SieveHub sieveHub) {
		this.hub = sieveHub;
	}

	public void append(PdfFile pdfFile) {
		pdfFileMap.putIfAbsent(pdfFile.getId(), pdfFile);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("PdfWorker");
		do {
			if (pdfFileMap.isEmpty()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			for (Map.Entry<String, PdfFile> pEntry : pdfFileMap.entrySet()) {
				String hash = pEntry.getKey();
				PdfFile pdfFile = pEntry.getValue();

				byte[] data = pdfFile.getData();
				try {
					try (PDDocument doc = PDDocument.load(data)) {
						String text = new PDFTextStripper().getText(doc);

						ArrayList<SieveTag> tags = createTagsByText(pdfFile, text);

						LOGGER.info("tags count:{}", tags.size());
						for (SieveTag sieveTag : tags) {
							if (!sieveTag.getPdfFilesIds().contains(pdfFile.getId())) {
								sieveTag.getPdfFilesIds().add(pdfFile.getId());
							}
							hub.getDb().save(sieveTag);
						}
						hub.getDb().save(pdfFile);
						pdfFileMap.remove(hash);
					}
				} catch (Exception e) {
					LOGGER.error("error DATA:{}", Arrays.toString(data), e);
					pdfFileMap.remove(hash);
				}

			}
			if (pdfFileMap.isEmpty()) {
				LOGGER.info("TASK END");
			}

		} while (true);

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

}
