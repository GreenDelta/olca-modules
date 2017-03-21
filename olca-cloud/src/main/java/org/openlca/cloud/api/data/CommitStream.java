package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openlca.cloud.model.data.BinaryFile;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.core.database.Daos;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CommitStream extends InputStream {

	private static final Logger log = LoggerFactory.getLogger(CommitStream.class);
	static final Charset CHARSET = Charset.forName("utf-8");
	private final IDatabase db;
	private final Gson gson = new Gson();
	// Reading from DB and converting to JSON is expected to be faster than
	// reading the data (and streaming it to the server), so the bufferQueue is
	// only used to parallelize reading/converting and streaming
	private final BlockingQueue<byte[]> bufferQueue = new ArrayBlockingQueue<>(1);
	private byte[] buffer = new byte[0];
	private int position = 0;
	private boolean doneAdding = false;

	public CommitStream(IDatabase db, String commitMessage, List<Dataset> datasets) {
		this.db = db;
		byte[] commitMessageBytes = commitMessage.getBytes(CHARSET);
		buffer = new byte[4 + 4 + commitMessageBytes.length];
		addTo(buffer, 0, asByteArray(datasets.size()));
		addTo(buffer, 4, asByteArray(commitMessageBytes.length));
		addTo(buffer, 8, commitMessageBytes);
		new Thread(() -> addAll(datasets)).start();
	}

	@Override
	public int read() throws IOException {
		if (position == buffer.length) {
			if (doneAdding && bufferQueue.isEmpty())
				return -1;
			try {
				buffer = bufferQueue.take();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			position = 0;
		}
		return buffer[position++] & 0xff;
	}

	private void addAll(List<Dataset> all) {
		for (Dataset dataset : all) {
			try {
				RootEntity entity = Daos.createRootDao(db, dataset.type).getForRefId(dataset.refId);
				byte[] dsJson = gson.toJson(dataset).getBytes(CHARSET);
				dsJson = BinUtils.gzip(dsJson);
				byte[] data = new byte[0];
				if (entity != null) {
					JsonObject object = JsonExport.toJson(entity);
					String json = gson.toJson(object);
					data = json.getBytes(CHARSET);
					data = BinUtils.gzip(data);
				}
				List<BinaryFile> binaryData = getBinaryFiles(dataset);
				byte[] buffer = buildBuffer(dsJson, data, binaryData);
				bufferQueue.put(buffer);
			} catch (Exception e) {
				log.error("Error adding model to model input stream", e);
			}
		}
		doneAdding = true;
	}

	private List<BinaryFile> getBinaryFiles(Dataset ds) throws IOException {
		FileStore fs = new FileStore(db.getFileStorageLocation());
		File dir = fs.getFolder(ds.type, ds.refId);
		if (dir == null || !dir.exists() || dir.list() == null)
			return new ArrayList<>(0);
		Read read = new Read(dir.toPath());
		Files.walkFileTree(dir.toPath(), read);
		return read.result;
	}

	private byte[] buildBuffer(byte[] dsJson, byte[] json, List<BinaryFile> binaryData) throws IOException {
		byte[] buffer = new byte[getLength(dsJson, json, binaryData)];
		int index = addTo(buffer, 0, asByteArray(dsJson.length));
		index = addTo(buffer, index, dsJson);
		index = addTo(buffer, index, asByteArray(json.length));
		index = addTo(buffer, index, json);
		index = addTo(buffer, index, asByteArray(binaryData.size()));
		for (BinaryFile entry : binaryData) {
			byte[] path = entry.path.getBytes(CHARSET);
			index = addTo(buffer, index, asByteArray(path.length));
			index = addTo(buffer, index, path);
			index = addTo(buffer, index, asByteArray(entry.data.length));
			index = addTo(buffer, index, entry.data);
		}
		return buffer;
	}

	private int getLength(byte[] dsJson, byte[] json, List<BinaryFile> binaryData) {
		int length = 4 + dsJson.length + 4 + json.length + 4;
		for (BinaryFile file : binaryData) {
			length += 4 + file.path.getBytes(CHARSET).length;
			length += 4 + file.data.length;
		}
		return length;
	}

	private int addTo(byte[] array, int index, byte... bytes) {
		for (int i = 0; i < bytes.length; i++) {
			array[index + i] = bytes[i];
		}
		return index + bytes.length;
	}

	private byte[] asByteArray(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}

	private class Read extends SimpleFileVisitor<Path> {

		private final Path dbDir;
		private final List<BinaryFile> result = new ArrayList<>();

		private Read(Path dbDir) {
			this.dbDir = dbDir;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			String path = dbDir.relativize(file).toString().replace('\\', '/');
			byte[] data = Files.readAllBytes(file);
			data = BinUtils.gzip(data);
			result.add(new BinaryFile(path, data));
			return FileVisitResult.CONTINUE;
		}

	}

}