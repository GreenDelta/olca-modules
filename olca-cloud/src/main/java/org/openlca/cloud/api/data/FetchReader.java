package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

public class FetchReader extends DataReader {

	private String commitId;

	public FetchReader(File zipFile) throws IOException {
		super(zipFile);
	}

	@Override
	protected void readMetaData(FileSystem zip) throws IOException {
		commitId = new String(Files.readAllBytes(zip.getPath("id.txt")));
	}

	public String getCommitId() {
		return commitId;
	}

}
