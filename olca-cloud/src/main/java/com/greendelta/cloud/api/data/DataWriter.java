package com.greendelta.cloud.api.data;

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

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greendelta.cloud.model.data.DatasetDescriptor;
import com.greendelta.cloud.util.Directories;

abstract class DataWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private File entityTmpFile;
	private EntityStore entityStore;
	private File descriptorTmpFile;
	private EntityStore descriptorStore;
	private JsonExport export;
	private File file;

	DataWriter(IDatabase database) {
		Path dir = null;
		try {
			dir = Files.createTempDirectory("commitWriter");
			entityTmpFile = new File(dir.toFile(), "entityStore.zip");
			entityStore = ZipStore.open(entityTmpFile);
			descriptorTmpFile = new File(dir.toFile(), "descriptorStore.zip");
			descriptorStore = ZipStore.open(descriptorTmpFile);
			file = new File(dir.toFile(), "commit.zip");
			export = new JsonExport(database, entityStore);
		} catch (IOException e) {
			log.error("Error creating temp files for commit writer", e);
			if (dir != null)
				Directories.delete(dir.toFile());
		}
	}

	public EntityStore getEntityStore() {
		return entityStore;
	}

	public void put(CategorizedEntity entity) {
		DatasetDescriptor descriptor = new DatasetDescriptor();
		descriptor.setLastChange(entity.getLastChange());
		descriptor.setRefId(entity.getRefId());
		descriptor.setName(entity.getName());
		descriptor.setType(ModelType.forModelClass(entity.getClass()));
		descriptor.setVersion(new Version(entity.getVersion()).toString());
		if (entity.getCategory() != null)
			descriptor.setCategoryRefId(entity.getCategory().getRefId());
		if (entity instanceof Category)
			descriptor.setCategoryType(((Category) entity).getModelType());
		else
			descriptor.setCategoryType(ModelType.forModelClass(entity
					.getClass()));
		descriptor.setFullPath(getFullPath(entity));
		export.write(entity);
		putDescriptor(descriptor);
	}

	public void put(DatasetDescriptor descriptor, String data) {
		if (data != null && !data.isEmpty()) {
			JsonElement element = new Gson().fromJson(data, JsonElement.class);
			JsonObject object = element.isJsonObject() ? element
					.getAsJsonObject() : null;
			entityStore.put(descriptor.getType(), object);
		}
		putDescriptor(descriptor);
	}

	private String getFullPath(CategorizedEntity entity) {
		String path = entity.getName();
		Category category = entity.getCategory();
		while (category != null) {
			path = category.getName() + "/" + path;
			category = category.getCategory();
		}
		return path;
	}

	private void putDescriptor(DatasetDescriptor descriptor) {
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
