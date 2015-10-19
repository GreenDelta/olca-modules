package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.openlca.core.database.IDatabase;

public class FetchWriter extends DataWriter {

	private String commitId;

	public FetchWriter(IDatabase database) {
		super(database);
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	@Override
	protected void writeMetaData(FileSystem zip) throws IOException {
		Files.write(zip.getPath("id.txt"), commitId.getBytes(),
				StandardOpenOption.CREATE);
	}

	@Override
	public File getFile() {
		return super.getFile();
	}

}
