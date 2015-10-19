package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

public class CommitReader extends DataReader {

	private String commitMessage;

	public CommitReader(File zipFile) throws IOException {
		super(zipFile);
	}

	@Override
	protected void readMetaData(FileSystem zip)  throws IOException {
		commitMessage = new String(Files.readAllBytes(zip.getPath("message.txt")));
	}

	public String getCommitMessage() {
		return commitMessage;
	}

}
