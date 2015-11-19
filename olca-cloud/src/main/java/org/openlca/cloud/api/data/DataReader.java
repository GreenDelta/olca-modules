package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.cloud.model.data.DatasetDescriptor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.ZipStore;

import com.google.gson.Gson;

abstract class DataReader {

	private File entityTmpFile;
	private File descriptorTmpFile;
	EntityStore entityStore;
	EntityStore descriptorStore;

	DataReader(File zipFile) throws IOException {
		String uriStr = zipFile.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		Map<String, String> options = new HashMap<>();
		FileSystem zip = FileSystems.newFileSystem(uri, options);
		entityTmpFile = File.createTempFile("entityStore", ".zip");
		Files.copy(zip.getPath("entityStore.zip"), entityTmpFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		entityStore = ZipStore.open(entityTmpFile);
		descriptorTmpFile = File.createTempFile("descriptorStore", ".zip");
		Files.copy(zip.getPath("descriptorStore.zip"),
				descriptorTmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		descriptorStore = ZipStore.open(descriptorTmpFile);
		readMetaData(zip);
		zip.close();
	}

	protected void readMetaData(FileSystem zip) throws IOException {

	}

	public List<DatasetDescriptor> getDescriptors() {
		List<DatasetDescriptor> descriptors = new ArrayList<>();
		for (ModelType type : ModelType.values())
			for (String refId : descriptorStore.getRefIds(type)) {
				DatasetDescriptor descriptor = getDescriptor(type, refId);
				if (descriptor != null)
					descriptors.add(descriptor);
			}
		return descriptors;
	}

	private DatasetDescriptor getDescriptor(ModelType type, String refId) {
		return new Gson().fromJson(descriptorStore.get(type, refId),
				DatasetDescriptor.class);
	}

	public void close() throws IOException {
		entityStore.close();
		entityTmpFile.delete();
		descriptorStore.close();
		descriptorTmpFile.delete();
	}
}
