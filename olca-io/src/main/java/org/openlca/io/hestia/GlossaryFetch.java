package org.openlca.io.hestia;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.util.Dirs;


/// Downloads glossary files from the Hestia API into a local folder.
/// Only downloads files that don't already exist in the target folder.
public class GlossaryFetch implements AutoCloseable {

	private final String BASE_URL = "http://hestia.earth";
	private final HestiaClient client;
	private final HttpClient http;
	private final File folder;
	private final ExecutorService pool;

	private GlossaryFetch(HestiaClient client, File folder) {
		this.client = client;
		this.folder = folder;
		this.http = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		this.pool = Executors.newFixedThreadPool(4);
	}

	public static Res<List<File>> run(HestiaClient client, File folder) {
		try {
			Dirs.createIfAbsent(folder);
			try (var fetch = new GlossaryFetch(client, folder)) {
				return fetch.exec();
			}
		} catch (Exception e) {
			return Res.error("Unexpected error in glossary download", e);
		}
	}

	private Res<List<File>> exec() {
		var infos = client.getGlossaryFileInfos();
		if (infos.isError())
			return infos.wrapError("Failed to get glossary file information");

		// start workers
		var files = new ArrayList<File>();
		var futures = new ArrayList<CompletableFuture<Res<File>>>();
		for (var info : infos.value()) {
			if (canFetch(info)) {
				var future = CompletableFuture.supplyAsync(
					() -> new Worker(info).download(), pool);
				futures.add(future);
			}
		}

		// wait for downloads and collect results
		try {
			for (var future : futures) {
				var result = future.get();
				if (result.isError())
					return result.wrapError("Download failed");
				files.add(result.value());
			}
		} catch (Exception e) {
			return Res.error("Error waiting for downloads to complete", e);
		}

		return Res.ok(files);
	}

	/// A glossary file can be downloaded if it has the required information
	/// and if it does not exist in the download folder.
	private boolean canFetch(GlossaryFileInfo info) {
		if (info == null
			|| Strings.isBlank(info.filename())
			|| Strings.isBlank(info.filepath()))
			return false;
		var file = new File(folder, info.filename());
		return !file.exists();
	}

	@Override
	public void close() {
		try {
			pool.shutdown();
			if (!pool.awaitTermination(5, TimeUnit.MINUTES)) {
				pool.shutdownNow();
			}
		} catch (InterruptedException e) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
		http.close();
	}

	private class Worker {

		private final GlossaryFileInfo info;

		Worker(GlossaryFileInfo info) {
			this.info = info;
		}

		Res<File> download() {
			var path = info.filepath();
			var url = !path.startsWith("/")
				? BASE_URL + "/" + path
				: BASE_URL + path;

			try {

				// fetch the file
				var req = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("accept", "*/*")
					.GET()
					.build();
				var resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
				if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
					return Res.error("Failed to download file from " + url +
						": " + resp.statusCode());
				}

				var target = new File(folder, info.filename());
				try (var inputStream = resp.body()) {
					Files.copy(inputStream, target.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				}
				return Res.ok(target);
			} catch (IOException | InterruptedException e) {
				return Res.error("Failed to download file from " + url, e);
			}
		}

	}

}
