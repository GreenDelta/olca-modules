package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.jsonld.EntityStore;

public class FetchReader extends DataReader {

	private String commitId;

	public FetchReader(File zipFile) throws IOException {
		super(zipFile);
	}

	public boolean hasData(Dataset descriptor) {
		return entityStore.contains(descriptor.type, descriptor.refId);
	}

	@Override
	protected void readMetaData(FileSystem zip) throws IOException {
		commitId = new String(Files.readAllBytes(zip.getPath("id.txt")));
	}

	public String getCommitId() {
		return commitId;
	}

	public EntityStore getEntityStore() {
		return entityStore;
	}

}
