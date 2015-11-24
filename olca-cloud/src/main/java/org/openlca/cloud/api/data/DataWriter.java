package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.openlca.cloud.model.data.DatasetDescriptor;
import org.openlca.cloud.util.Directories;
import org.openlca.core.database.IDatabase;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

abstract class DataWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private File entityTmpFile;
	private File descriptorTmpFile;
	private File file;
	EntityStore entityStore;
	EntityStore descriptorStore;

	DataWriter(IDatabase database) {
		Path dir = null;
		try {
			dir = Files.createTempDirectory("commitWriter");
			entityTmpFile = new File(dir.toFile(), "entityStore.zip");
			entityStore = ZipStore.open(entityTmpFile);
			descriptorTmpFile = new File(dir.toFile(), "descriptorStore.zip");
			descriptorStore = ZipStore.open(descriptorTmpFile);
			file = new File(dir.toFile(), "commit.zip");
		} catch (IOException e) {
			log.error("Error creating temp files for commit writer", e);
			if (dir != null)
				Directories.delete(dir.toFile());
		}
	}

	void putDescriptor(DatasetDescriptor descriptor) {
		JsonObject element = (JsonObject) new Gson().toJsonTree(descriptor);
		element.addProperty("@id", descriptor.getRefId());
		descriptorStore.put(descriptor.getType(), element);
	}

	public void close() throws IOException {
		String uriStr = file.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		Map<String, String> options = new HashMap<>();
		if (!file.exists())
			options.put("create", "true");
		FileSystem zip = null;
		try {
			zip = FileSystems.newFileSystem(uri, options);
			writeMetaData(zip);
			entityStore.close();
			copyFile(zip, "entityStore.zip", entityTmpFile);
			descriptorStore.close();
			copyFile(zip, "descriptorStore.zip", descriptorTmpFile);
		} finally {
			if (zip != null)
				zip.close();
		}
	}

	protected void writeMetaData(FileSystem zip) throws IOException {

	}

	protected File getFile() {
		return file;
	}

	private void copyFile(FileSystem zip, String entryName, File tmpFile)
			throws IOException {
		Path file = zip.getPath(entryName);
		Path dir = file.getParent();
		if (dir != null && !(Files.exists(dir)))
			Files.createDirectories(dir);
		Files.copy(tmpFile.toPath(), file, StandardCopyOption.REPLACE_EXISTING);
		tmpFile.delete();
	}

}
