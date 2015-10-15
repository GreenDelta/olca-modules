package com.greendelta.cloud.api.data;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.openlca.core.database.IDatabase;

public class CommitWriter extends DataWriter {

	private String commitMessage;

	public CommitWriter(IDatabase database) {
		super(database);
	}

	public void setCommitMessage(String message) {
		this.commitMessage = message;
	}

	@Override
	protected void writeMetaData(FileSystem zip) throws IOException {
		if (commitMessage == null)
			commitMessage = "";
		Files.write(zip.getPath("message.txt"), commitMessage.getBytes(),
				StandardOpenOption.CREATE);
	}

}
