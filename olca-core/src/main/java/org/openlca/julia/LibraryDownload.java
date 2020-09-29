package org.openlca.julia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.util.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tries to download the OS specific native libraries and put them into the
 * native library folder.
 */
class LibraryDownload {

	private final Logger log = LoggerFactory.getLogger(getClass());

	void run() throws Exception {
		var dir = Julia.getDefaultDir();
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new IOException("could not create " + dir);
		}

		var path = web();
		if (path == null)
			throw new IllegalStateException("unsupported OS");

		try {
			URL url = new URL(path);
			try (InputStream in = url.openStream()) {
				var temp = Files.createTempFile("olcar_1.0.0_", ".zip");
				log.info("download libraries from {} to {}", path, temp);
				Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
				extract(temp.toFile(), dir);
			}
		} catch (Exception e) {
			log.error("failed to download native libraries from " + path, e);
			throw e;
		}
	}

	private void extract(File zipFile, File dir) throws Exception {
		log.info("extract library package {}", zipFile);
		try (var zip = new ZipFile(zipFile)) {
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var e = entries.nextElement();
				if (e.isDirectory())
					continue;
				var target = new File(dir, e.getName());
				if (target.exists()) {
					log.info("file {} already exists", target);
					continue;
				}
				try (var in = zip.getInputStream(e)) {
					log.info("copy library {}", target);
					Files.copy(in, target.toPath());
				}
			}
		}
	}

	private String web() {
		var base = "https://github.com/msrocka/olca-rust/releases/download/" +
				"v1.1.0/olcar_withumf_1.1.0_";
		switch (OS.get()) {
			case MAC:
				return base + "macos_2020-09-29.zip";
			case WINDOWS:
				return base + "windows_2020-09-29.zip";
			case LINUX:
				return base + "linux_2020-09-29.zip";
			default:
				return null;
		}
	}
}
