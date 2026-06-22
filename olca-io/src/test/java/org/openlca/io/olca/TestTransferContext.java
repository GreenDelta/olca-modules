package org.openlca.io.olca;

import java.io.IOException;
import java.nio.file.Files;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;

public class TestTransferContext {

	private static final Derby source;
	private static final Derby target;

	static {
		try {
			source = Derby.createInMemory();
			var srcDir = Files.createTempDirectory("olca-test-src").toFile();
			source.setFileStorageLocation(srcDir);

			target = Derby.createInMemory();
			var tgtDir = Files.createTempDirectory("olca-test-tgt").toFile();
			target.setFileStorageLocation(tgtDir);
		} catch (IOException e) {
			throw new RuntimeException(
				"Failed to create in-memory databases with ", e);
		}
	}

	public static TestTransferContext get() {
		return new TestTransferContext();
	}

	public IDatabase source() {
		return source;
	}

	public IDatabase target() {
		return target;
	}

	public void clear() {
		source.clear();
		target.clear();
	}
}
