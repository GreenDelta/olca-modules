package org.openlca.io.ecospold2.input;

import org.openlca.util.ZipFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spold2.DataSet;
import spold2.EcoSpold2;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class DataSetIterator implements Iterator<DataSet>, Closeable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private DataSet next;
	private File[] files;
	private int currentFile;

	private ZipFile zipFile;
	private int currentZipEntry;
	private ZipEntry[] zipEntries;

	public DataSetIterator(File[] files) {
		log.trace("initialize data set iterator");
		this.files = files;
		currentFile = 0;
		moveNext();
	}

	@Override
	public void close() {
		next = null;
		files = null;
		closeZip();
	}

	@Override
	public void remove() {
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public DataSet next() {
		DataSet current = next;
		moveNext();
		return current;
	}

	private void moveNext() {
		if (zipFile != null) {
			moveNextInZip();
			return;
		}
		next = null;
		if (files == null || currentFile >= files.length) {
			log.trace("no more files");
			return;
		}
		log.trace("move data set pointer in files");
		File file = files[currentFile];
		currentFile++;
		if (isSpoldFile(file.getName()))
			nextSpold(file);
		else if (isZip(file)) {
			openZip(file);
			moveNextInZip();
		} else {
			log.trace("ignore file {}", file);
			moveNext();
		}
	}

	private void nextSpold(File file) {
		try {
			next = EcoSpold2.read(file).activity();
		} catch (Exception e) {
			log.error("failed to read spold file " + file, e);
			moveNext();
		}
	}

	private void openZip(File file) {
		log.trace("open zip file {}", file);
		try {
			currentZipEntry = 0;
			zipFile = ZipFiles.open(file);
			var entries = zipFile.entries();
			var spoldEntries = new ArrayList<ZipEntry>();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				if (entry.isDirectory())
					continue;
				if (isSpoldFile(entry.getName()))
					spoldEntries.add(entry);
			}
			this.zipEntries = spoldEntries.toArray(new ZipEntry[0]);
		} catch (Exception e) {
			log.error("failed to open Zip file " + file, e);
			closeZip();
		}
	}

	private void moveNextInZip() {
		next = null;
		if (zipFile == null || zipEntries == null
				|| currentZipEntry >= zipEntries.length) {
			log.trace("no  more zip entries");
			moveNext();
		}
		log.trace("move data set pointer in zip entries");
		ZipEntry entry = zipEntries[currentZipEntry];
		currentZipEntry++;
		try {
			next = EcoSpold2.read(zipFile.getInputStream(entry)).activity();
			if (currentZipEntry >= zipEntries.length)
				closeZip();
		} catch (Exception e) {
			log.error("failed to open zip entry", e);
		}
	}

	private void closeZip() {
		if (zipFile == null)
			return;
		try {
			zipFile.close();
		} catch (Exception e) {
			log.error("failed to close zip file " + zipFile, e);
		} finally {
			zipFile = null;
			zipEntries = null;
			currentZipEntry = 0;
		}
	}

	private boolean isSpoldFile(String name) {
		if (name == null)
			return false;
		return name.toLowerCase().endsWith(".spold");
	}

	private boolean isZip(File file) {
		if (file == null)
			return false;
		return file.getName().toLowerCase().endsWith(".zip");
	}

}
