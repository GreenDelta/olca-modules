package org.openlca.io.olca;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;

public class ExternalFileTest {

	private final TestTransferContext ctx = TestTransferContext.get();

	@After
	public void cleanup() {
		ctx.clear();
	}

	@Test
	public void testCopySourceWithFiles() throws Exception {
		// create a source with external file
		var source = Source.of("Test Source");
		source.externalFile = "test.txt";

		var folder = FileStore.of(ctx.source())
			.orElseThrow()
			.getFolder(ModelType.SOURCE, source.refId);
		Files.createDirectories(folder.toPath());
		var file = folder.toPath().resolve(source.externalFile);
		Files.writeString(file, "test content");

		// insert the source and transfer it
		ctx.source().insert(source);
		var copy = TransferContext.create(ctx.source(), ctx.target())
			.resolve(source);
		assertEquals(source.refId, copy.refId);
		assertEquals(source.externalFile, copy.externalFile);

		// check that the file was transferred too
		var targetFile = FileStore.of(ctx.target())
			.orElseThrow()
			.getFolder(copy)
			.toPath()
			.resolve(copy.externalFile);

		assertTrue(Files.exists(targetFile));
		assertEquals("test content", Files.readString(targetFile));
	}
}
