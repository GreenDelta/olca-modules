package org.openlca.cloud.api.data;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.util.BinUtils;

import com.google.gson.Gson;

public class ModelStreamReader implements Closeable {

	private final Gson gson = new Gson();
	private final InputStream stream;
	private final int total;
	private int read;

	public ModelStreamReader(InputStream stream) throws IOException {
		this.stream = stream;
		this.total = readNextInt();
	}

	public String readNextPartAsString() throws IOException {
		return new String(readNextPart(), ModelStream.CHARSET);
	}

	public boolean hasMore() {
		return read < total;
	}

	public int getTotal() {
		return total;
	}

	public Dataset readNextPartAsDataset() throws IOException {
		byte[] dataset = readNextPart();
		read++;
		dataset = BinUtils.gunzip(dataset);
		String json = new String(dataset, ModelStream.CHARSET);
		return gson.fromJson(json, Dataset.class);
	}

	public int readNextPartToStream(OutputStream out) throws IOException {
		int length = readNextInt();
		return readBytes(length, out);
	}

	public byte[] readNextPart() throws IOException {
		int length = readNextInt();
		if (length == 0)
			return new byte[0];
		return readBytes(length);
	}

	public int readNextInt() throws IOException {
		byte[] bytes = readBytes(4);
		return ByteBuffer.wrap(bytes).getInt();
	}

	private byte[] readBytes(int length) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		readBytes(length, out);
		return out.toByteArray();
	}

	private int readBytes(int length, OutputStream out) throws IOException {
		if (length == 0)
			return 0;
		InputStream in = new FixedLengthInputStream(length);
		byte[] buffer = new byte[1024];
		int count = -1;
		int total = 0;
		while ((count = in.read(buffer)) >= 0) {
			out.write(buffer, 0, count);
			total += count;
		}
		in.close();
		return total;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	private class FixedLengthInputStream extends InputStream {

		private final int length;
		private int totalRead = 0;

		private FixedLengthInputStream(int length) {
			this.length = length;
		}

		@Override
		public int read() throws IOException {
			if (totalRead == length)
				return -1;
			totalRead++;
			return stream.read();
		}

	}

}