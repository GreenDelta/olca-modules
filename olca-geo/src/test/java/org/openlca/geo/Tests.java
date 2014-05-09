package org.openlca.geo;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Tests {

	private static ShapeFileRepository repository;

	private Tests() {
	}

	public static String getKml(String file) {
		try {
			return IOUtils.toString(Tests.class.getResourceAsStream(file),
					"utf-8");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Tests.class);
			log.error("failed to load kml " + file, e);
			return null;
		}
	}

	public static KmlFeature getKmlFeature(String file) {
		try {
			return KmlFeature.parse(getKml(file));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Tests.class);
			log.error("failed to load kml feature " + file, e);
			return null;
		}
	}

	public static ShapeFileRepository getRepository() {
		if (repository == null)
			repository = initRepository();
		return repository;
	}

	private static ShapeFileRepository initRepository() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File testDir = new File(tempDir, "olca_geo_test_dir");
		File repoDir = new File(testDir, "repo");
		if (!repoDir.exists()) {
			repoDir.mkdirs();
			extractSampleShapeFile(repoDir);
		}
		return new ShapeFileRepository(repoDir);
	}

	private static void extractSampleShapeFile(File repoDir) {
		try {
			File tempFile = Files.createTempFile("us_states_shp", ".zip")
					.toFile();
			FileOutputStream os = new FileOutputStream(tempFile);
			InputStream is = Tests.class
					.getResourceAsStream("us_states_shp.zip");
			IOUtils.copy(is, os);
			ZipFile zipFile = new ZipFile(tempFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(repoDir, entry.getName());
				FileOutputStream fos = new FileOutputStream(file);
				InputStream zis = zipFile.getInputStream(entry);
				IOUtils.copy(zis, fos);
				zis.close();
				fos.close();
			}
			zipFile.close();
			is.close();
			os.close();
			tempFile.delete();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Tests.class);
			log.error("failed to init test repo", e);
		}
	}

}
