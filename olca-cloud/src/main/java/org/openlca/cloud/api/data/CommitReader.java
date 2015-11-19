package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.openlca.cloud.model.data.DatasetDescriptor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CommitReader extends DataReader {

	private String commitMessage;

	public CommitReader(File zipFile) throws IOException {
		super(zipFile);
	}

	public String getData(DatasetDescriptor descriptor) {
		JsonObject element = entityStore.get(descriptor.getType(),
				descriptor.getRefId());
		if (element == null)
			return null;
		return new Gson().toJson(element);
	}

	public void copyBinaries(DatasetDescriptor descriptor, File binDir) throws IOException {
		List<String> paths = entityStore.getBinFiles(descriptor.getType(),
				descriptor.getRefId());
		if (paths.isEmpty())
			return;
		for (String path : paths)
			copyBinary(path, binDir);
	}

	private void copyBinary(String path, File binDir) throws IOException {
		byte[] data = entityStore.get(path);
		if (data == null)
			return;
		String fileName = Paths.get(path).getFileName().toString();
		File file = new File(binDir, fileName);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		Files.write(file.toPath(), data, StandardOpenOption.CREATE);
	}

	@Override
	protected void readMetaData(FileSystem zip) throws IOException {
		commitMessage = new String(Files.readAllBytes(zip
				.getPath("message.txt")));
	}

	public String getCommitMessage() {
		return commitMessage;
	}

}
