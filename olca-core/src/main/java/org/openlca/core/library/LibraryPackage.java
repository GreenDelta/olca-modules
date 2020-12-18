package org.openlca.core.library;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
			 var outBuff = new BufferedOutputStream(out);
			 var zip = new ZipOutputStream(outBuff)) {

			// put the library files directly in the root of
			// the zip
			var libFiles = library.folder.listFiles();
			if (libFiles == null)
				return;
			for (var libFile : libFiles) {
				zip.putNextEntry(new ZipEntry(libFile.getName()));
				try (var in = new FileInputStream(libFile);
					 var inBuff = new BufferedInputStream(in)) {
					inBuff.transferTo(zip);
				}
				zip.closeEntry();
			}

			// put the dependent libraries into the "dependencies"
			// sub-folder
			for (var depLib : library.getDependencies()) {
				var prefix = "dependencies/" + depLib.id() + "/";
				var depFiles = depLib.folder.listFiles();
				if (depFiles == null)
					continue;
				for (var depFile : depFiles) {
					var entry = new ZipEntry(prefix + depFile.getName());
					zip.putNextEntry(entry);
					try (var in = new FileInputStream(depFile);
						 var inBuff = new BufferedInputStream(in)) {
						inBuff.transferTo(zip);
					}
					zip.closeEntry();
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
