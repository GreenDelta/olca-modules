package org.openlca.io;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Currently only a Zip package is created, but we will add */
public final class DBPack {

	private DBPack() {
	}

	public static void createPackage(File databaseFolder, String outputPath) {
		try {
			ZipFile zipFile = new ZipFile(outputPath);
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zipFile.addFolder(databaseFolder, parameters);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(DBPack.class);
			log.error("failed to package database", e);
		}
	}

	public static void extractPackage(File zipFile, String destPath) {
		try {
			ZipFile zip = new ZipFile(zipFile);
			zip.extractAll(destPath);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(DBPack.class);
			log.error("failed to extract database", e);
		}
	}

}
