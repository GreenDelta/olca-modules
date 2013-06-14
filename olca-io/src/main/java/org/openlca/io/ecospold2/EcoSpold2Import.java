package org.openlca.io.ecospold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoSpold2Import {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProcessImport processImport;

	public EcoSpold2Import(IDatabase database) {
		this.processImport = new ProcessImport(database);
	}

	public void run(File[] files) {
		for (File file : files) {
			if (hasExtension(file, ".zip"))
				handleZip(file);
			else if (hasExtension(file, ".spold"))
				handleSpold(file);
		}
	}

	private boolean hasExtension(File file, String ext) {
		return file != null && hasExtension(file.getName(), ext);
	}

	private boolean hasExtension(String fileName, String ext) {
		return fileName != null && fileName.toLowerCase().trim().endsWith(ext);
	}

	private void handleZip(File file) {
		log.trace("import zip file {}", file);
		try (ZipFile zip = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry next = entries.nextElement();
				String name = next.getName();
				if (!next.isDirectory() && hasExtension(name, ".spold")) {
					log.trace("import zip entry {}", name);
					try (InputStream stream = zip.getInputStream(next)) {
						handleInput(stream);
					}
				}
			}
		} catch (Exception e) {
			log.error("Reading from ZIP failed " + file, e);
		}
	}

	private void handleSpold(File file) {
		log.trace("import spold file {}", file);
		try (FileInputStream stream = new FileInputStream(file)) {
			handleInput(stream);
		} catch (Exception e) {
			log.error("Reading from SPOLD failed " + file, e);
		}
	}

	private void handleInput(InputStream stream) throws Exception {
		processImport.importStream(stream);
	}
}
