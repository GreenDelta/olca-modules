package org.openlca.core.database;

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;

public class LibraryUsage {

	/// Checks all Derby databases that are located in the given root folder
	/// for the usage of the library with the given name. Returns those database
	/// folders where the library is used.
	public static Res<List<File>> allDatabasesOf(File root, String library) {
		if (root == null || !root.isDirectory())
			return Res.error("The provided root folder is not a directory");
		var dirs = root.listFiles();
		if (dirs == null)
			return Res.error("Could not access the provided root folder");
		var dbs = Arrays.stream(dirs)
			.filter(Derby::isDerbyFolder)
			.toList();
		return databasesOf(dbs, library);
	}

	/// Returns the list of Derby database folders where the library with the
	/// given name is used.
	public static Res<List<File>> databasesOf(List<File> folders, String library) {
		if (folders == null || folders.isEmpty())
			return Res.ok(List.of());
		if (Strings.isBlank(library))
			return Res.error("The library name is empty");

		// one folder to check -> no multi-threading
		if (folders.size() == 1) {
			var folder = folders.getFirst();
			var res = new DbCheck(folder, library).get();
			if (res.isError())
				return Res.error("Failed to check for library usage of: " + folder);
			var file = res.value().orElse(null);
			return Res.ok(file != null ? List.of(file) : List.of());
		}

		// multithreaded database check
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			var tasks = folders.stream()
				.map(folder -> new DbCheck(folder, library))
				.map(check -> CompletableFuture.supplyAsync(check, executor))
				.toList();
			Res<?> err = null;
			var files = new ArrayList<File>();
			for (var task : tasks) {
				var res = task.get();
				if (res.isError()) {
					err = err == null
						? res
						: res.wrapError(err.error());
					continue;
				}
				res.value().ifPresent(files::add);
			}
			return err == null
				? Res.ok(files)
				: err.wrapError("Failed to check databases for library usage");
		} catch (Exception e) {
			return Res.error("Failed to check databases for library usage", e);
		}
	}

	private record DbCheck(File folder, String library)
		implements Supplier<Res<Optional<File>>> {

		public Res<Optional<File>> get() {
			if (!Derby.isDerbyFolder(folder))
				return Res.error("Not a valid database folder: " + folder);
			var url = "jdbc:derby:" + folder.getAbsolutePath().replace('\\', '/');
			try (var con = DriverManager.getConnection(url)){
				var libs = IDatabase.getLibraries(con);
				return libs.contains(library)
					? Res.ok(Optional.of(folder))
					: Res.ok(Optional.empty());
			} catch (Exception e) {
				return Res.error("Failed to query database for libraries: " + folder, e);
			} finally {
				try {
					DriverManager
						.getConnection(url + ";shutdown=true")
						.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
}
