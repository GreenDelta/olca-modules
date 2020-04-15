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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import org.openlca.cloud.model.data.BinaryFile;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class ModelStream<T extends Dataset> extends InputStream {

	private static final Logger log = LoggerFactory.getLogger(CommitStream.class);
	public static final Charset CHARSET = Charset.forName("utf-8");
	private final Gson gson = new Gson();
	// Reading and converting is expected to be faster than
	// reading the data (and streaming it to the server), so the bufferQueue is
	// only used to parallelize reading/converting and streaming
	private final BlockingQueue<byte[]> bufferQueue = new ArrayBlockingQueue<>(10);
	// Used to identify which is the current dataset in the bufferQueue
	private final Queue<T> datasetQueue = new LinkedList<>();
	private final Consumer<T> callback;
	private final int total;
	private int count;
	private byte[] buffer = new byte[0];
	private int position = 0;

	protected ModelStream(String message, Iterator<T> datasets, int total) {
		this(message, datasets, total, null);
	}

	protected ModelStream(String message, Iterator<T> datasets, int total, Consumer<T> callback) {
		this.callback = callback;
		byte[] messageBytes = message.getBytes(CHARSET);
		buffer = new byte[4 + 4 + messageBytes.length];
		addTo(buffer, 0, asByteArray(total));
		addTo(buffer, 4, asByteArray(messageBytes.length));
		addTo(buffer, 8, messageBytes);
		this.total = total;
		if (total > 0) {
			new Thread(() -> addAll(datasets)).start();			
		}
	}

	@Override
	public final int read() throws IOException {
		if (position == buffer.length) {
			if (total == count && bufferQueue.isEmpty())
				return -1;
			try {
				if (callback != null && !datasetQueue.isEmpty()) {
					callback.accept(datasetQueue.poll());
				}
				buffer = bufferQueue.take();
				count++;
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			position = 0;
		}
		return buffer[position++] & 0xff;
	}

	private void addAll(Iterator<T> all) {
		while (all.hasNext()) {
			T dataset = all.next();
			try {
				byte[] dsJson = gson.toJson(dataset).getBytes(CHARSET);
				dsJson = BinUtils.gzip(dsJson);
				byte[] data = getData(dataset);
				List<BinaryFile> binaryData = getBinaryFiles(dataset);
				byte[] buffer = buildBuffer(dsJson, data, binaryData);
				if (callback != null) {
					datasetQueue.add(dataset);
				}
				bufferQueue.put(buffer);
			} catch (Exception e) {
				log.error("Error adding model to stream", e);
			}
		}
	}

	protected abstract byte[] getData(T dataset) throws IOException;

	private List<BinaryFile> getBinaryFiles(T dataset) throws IOException {
		File dir = getBinaryFilesLocation(dataset);
		if (dir == null || !dir.exists() || dir.list() == null)
			return new ArrayList<>(0);
		Read read = new Read(dir.toPath());
		Files.walkFileTree(dir.toPath(), read);
		return read.result;
	}

	protected abstract File getBinaryFilesLocation(T dataset);

	private byte[] buildBuffer(byte[] dsJson, byte[] json, List<BinaryFile> binaryData) throws IOException {
		byte[] buffer = new byte[getLength(dsJson, json, binaryData)];
		int index = addTo(buffer, 0, asByteArray(dsJson.length));
		index = addTo(buffer, index, dsJson);
		index = addTo(buffer, index, asByteArray(json.length));
		if (json.length == 0)
			return buffer;
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
		int length = 4 + dsJson.length + 4 + json.length;
		if (json.length == 0)
			return length;
		length += 4;
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

	protected abstract byte[] getBinaryData(Path file) throws IOException;

	private class Read extends SimpleFileVisitor<Path> {

		private final Path dir;
		private final List<BinaryFile> result = new ArrayList<>();

		private Read(Path dir) {
			this.dir = dir;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String path = dir.relativize(file).toString().replace('\\', '/');
			if (path.endsWith(".gz")) {
				path = path.substring(0, path.length() - 3);
			}
			byte[] data = getBinaryData(file);
			result.add(new BinaryFile(path, data));
			return FileVisitResult.CONTINUE;
		}

	}

}
