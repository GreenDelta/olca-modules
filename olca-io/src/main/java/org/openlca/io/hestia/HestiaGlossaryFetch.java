package org.openlca.io.hestia;

import java.io.File;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.util.Dirs;

// TODO: It should download all glossary files into a folder.
// First, it fetches the file information from the API client.
// For each file, it first checks if the folder already contains
// that file. If not, it starts a download worker. It should use
// a HTTP client for the download. Downloads will run in multiple
// threads using a fixed threadpool of size n.

public class HestiaGlossaryFetch implements AutoCloseable {

	private final String BASE_URL = "http://hestia.earth";
	private final HestiaClient client;
	private final HttpClient http;
	private final File folder;

	private HestiaGlossaryFetch(HestiaClient client, File folder) {
		this.client = client;
		this.folder = folder;
		this.http = HttpClient.newHttpClient();
	}

	public static Res<List<File>> run(HestiaClient client, File folder) {
		try {
			Dirs.createIfAbsent(folder);
			try (var fetch = new HestiaGlossaryFetch(client, folder)) {
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
		var files = new ArrayList<File>();
		for (var info : infos.value()) {
			if (canFetch(info)) {
				// start a new worker to download the file
				// if a worker failes, let the complete fetch fail
			}
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
		// TODO close the HTTP client
		// maybe also the worker pool
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
			// TODO download the file

			return Res.error("not yet implemented");
		}

	}

}
