package org.openlca.core.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LibraryPackage {

	/**
	 * Writes the given library with all of its dependencies into the given
	 * ZIP file.
	 */
	public static void zip(Library library, File zipFile) {
		if (library == null
			|| library.folder == null
			|| zipFile == null)
			return;

		try (var out = new FileOutputStream(zipFile);
			 var zip = new ZipOutputStream(out)) {

			// put the library files directly in the root of
			// the zip
			var libFiles = library.folder.listFiles();
			if (libFiles == null)
				return;
			for (var libFile : libFiles) {
				zip.putNextEntry(new ZipEntry(libFile.getName()));
				try (var in = new FileInputStream(libFile)) {
					in.transferTo(zip);
				}
				zip.closeEntry();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
