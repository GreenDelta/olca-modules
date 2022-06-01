package org.openlca.core.library;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.openlca.jsonld.Json;

public class LibraryPackage {

	/**
	 * Writes the given library with all of its dependencies into the given
	 * ZIP file.
	 */
	public static void zip(Library library, File zipFile) {
		if (library == null
			|| library.folder() == null
			|| zipFile == null)
			return;

		try (var out = new FileOutputStream(zipFile);
			 var outBuff = new BufferedOutputStream(out);
			 var zip = new ZipOutputStream(outBuff)) {

			// put the library files directly in the root of
			// the zip
			var libFiles = library.folder().listFiles();
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
			for (var depLib : library.getTransitiveDependencies()) {
				var prefix = "dependencies/" + depLib.name() + "/";
				var depFiles = depLib.folder().listFiles();
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

	/**
	 * Get the library information from the given zip file. Returns `null` if
	 * this is not a valid library file.
	 */
	public static LibraryInfo getInfo(File zipFile) {
		if (zipFile == null || !zipFile.exists())
			return null;
		try (var zip = new ZipFile(zipFile)) {
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				if (!"library.json".equals(entry.getName()))
					continue;
				try (var stream = zip.getInputStream(entry)) {
					var json = Json.readObject(stream);
					return json.isEmpty()
						? null
						: LibraryInfo.fromJson(json.get());
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Extracts the library and its dependencies of the given library package
	 * into the given library folder. If the library or a dependency already
	 * exists in that library it is simply ignored.
	 */
	public static void unzip(File zipFile, LibraryDir libDir) {
		if (zipFile == null || libDir == null)
			return;
		var info = getInfo(zipFile);
		if (info == null)
			throw new IllegalArgumentException(
				zipFile + " is not a library package");

		// do nothing when the library already exists
		var libId = info.name();
		if (libDir.hasLibrary(libId))
			return;

		// collect the dependencies that we need to copy
		var deps = info.dependencies().stream()
			.filter(dep -> libDir.getLibrary(dep).isEmpty())
			.collect(Collectors.toSet());

		try (var zip = new ZipFile(zipFile)) {

			// create the target folders
			var lib = libDir.create(libId);
			info.writeTo(lib);

			for (var dep : deps) {
				var depDir = new File(libDir.folder(), dep);
				Files.createDirectories(depDir.toPath());
			}

			// extract the files
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				if (entry.isDirectory())
					continue;
				var path = entry.getName().split("[/\\\\]");
				if (path.length == 0)
					continue;

				File target;
				if ("dependencies".equals(path[0])) {
					// a dependency file
					if (path.length < 3)
						continue;
					var dep = path[1];
					if (!deps.contains(dep))
						continue;
					target = new File(libDir.folder(), dep + "/" + path[2]);
				} else {
					// a root library file
					target = new File(lib.folder(), path[0]);
				}

				// copy the content to the target file
				if (target.exists())
					continue;
				try (var stream = zip.getInputStream(entry)) {
					Files.copy(stream, target.toPath());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to extract library package", e);
		}
	}
}
