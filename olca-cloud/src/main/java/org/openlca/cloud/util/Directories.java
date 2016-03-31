package org.openlca.cloud.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Files;

public class Directories {

	public static boolean delete(File file) {
		if (!file.exists())
			return false;
		if (file.isDirectory())
			for (File child : file.listFiles())
				if (!delete(child))
					return false;
		return file.delete();
	}

	public static void streamZipped(File directory, OutputStream out) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(out);
		zipOut.setMethod(ZipEntry.DEFLATED);
		zipOut.setLevel(Deflater.BEST_COMPRESSION);
		for (File file : directory.listFiles())
			put(file, zipOut);
		zipOut.close();
	}

	private static void put(File file, ZipOutputStream stream) throws IOException {
		ZipEntry entry = new ZipEntry(file.getName());
		stream.putNextEntry(entry);
		Files.copy(file, stream);
		stream.closeEntry();
	}

}
