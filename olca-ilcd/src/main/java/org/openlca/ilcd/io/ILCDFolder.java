package org.openlca.ilcd.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ILCDFolder {

	private File rootDir;
	private File zipFile;

	public ILCDFolder(File rootDir) {
		this.rootDir = rootDir;
	}

	public void makeFolder() throws IOException {
		copyZip();
		unzip();
		removeZip();
	}

	private void copyZip() throws IOException {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"ilcd_folder.zip")) {
			String zipName = UUID.randomUUID().toString() + ".zip";
			zipFile = new File(rootDir, zipName);
			transfer(stream, zipFile);
		}
	}

	private void unzip() throws IOException {
		try (ZipFile zip = new ZipFile(zipFile)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				if (zipEntry.isDirectory()) {
					checkMakeDir(new File(rootDir, zipEntry.getName()));
				} else {
					File file = new File(rootDir, zipEntry.getName());
					checkMakeDir(file.getParentFile());
					if (!file.exists()) {
						try (InputStream stream = zip.getInputStream(zipEntry)) {
							transfer(stream, file);
						}
					}
				}
			}
		}
	}

	private void checkMakeDir(File dir) {
		if (!dir.exists())
			dir.mkdirs();
	}

	private void transfer(InputStream stream, File file) throws IOException {
		try (ReadableByteChannel source = Channels.newChannel(stream);
				FileOutputStream fos = new FileOutputStream(file);
				FileChannel target = fos.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			while (source.read(buffer) != -1) {
				buffer.flip();
				target.write(buffer);
				buffer.compact();
			}
			buffer.flip();
			while (buffer.hasRemaining()) {
				target.write(buffer);
			}
		}
	}

	private void removeZip() {
		boolean b = zipFile.delete();
		if (!b) {
			zipFile.deleteOnExit();
		}
	}

}
