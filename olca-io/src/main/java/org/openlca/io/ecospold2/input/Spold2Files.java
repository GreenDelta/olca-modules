package org.openlca.io.ecospold2.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spold2.EcoSpold2;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Spold2Files {

	private final Logger log = LoggerFactory.getLogger(Spold2Files.class);
	private final File[] files;
	private final Consumer<EcoSpold2> handler;

	private Spold2Files(File[] files, Consumer<EcoSpold2> handler) {
		this.files = files;
		this.handler = handler;
	}

	/**
	 * Iterates over the given files which can be spold, xml, or zip files with
	 * spold or xml files. For each EcoSpold2 data set found in these files it
	 * calls the given consumer.
	 */
	public static void parse(File[] files, Consumer<EcoSpold2> fn) {
		new Spold2Files(files, fn).run();
	}

	private void run() {
		if (files == null || files.length == 0 || handler == null) {
			log.info("no files or consumer given; nothing to do");
			return;
		}
		for (File file : files) {
			if (file == null)
				continue;
			if (file.getName().toLowerCase().endsWith(".zip")) {
				parseZip(file);
			} else if (isValidFileName(file.getName())) {
				parseFile(file);
			} else {
				log.warn("unknown file format: {};" +
						" expected *.xml, *.spold, or *.zip", file);
			}
		}
	}


	private void parseZip(File file) {
		log.info("parse entries in zip file {}", file);
		try (ZipFile zip = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory() || !isValidFileName(entry.getName())) {
					continue;
				}
				try (InputStream stream = zip.getInputStream(entry)) {
					EcoSpold2 es2 = EcoSpold2.read(stream);
					if (es2 != null) {
						handler.accept(es2);
					}
				} catch (Exception e) {
					log.warn("failed to parse zip entry {}", entry.getName());
				}
			}
		} catch (Exception e) {
			log.error("failed to read from zip file {}", file);
		}
	}

	private void parseFile(File file) {
		log.trace("parse file {}", file);
		try {
			EcoSpold2 es2 = EcoSpold2.read(file);
			if (es2 != null) {
				handler.accept(es2);
			}
		} catch (Exception e) {
			log.warn("failed to parse file {}" +
					" as EcoSpold 2 file", file);
		}
	}

	private boolean isValidFileName(String name) {
		if (name == null)
			return false;
		String lower = name.toLowerCase().trim();
		return lower.endsWith(".spold")
				|| lower.endsWith(".xml");
	}
}
