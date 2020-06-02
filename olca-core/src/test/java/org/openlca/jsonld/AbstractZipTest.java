package org.openlca.jsonld;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class AbstractZipTest {

	private File tempDir;
	protected File zipFile;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("olca-json_").toFile();
		zipFile = new File(tempDir, "test_zip.zip");
	}

	@After
	public void tearDown() throws Exception {
		Assert.assertTrue(zipFile.delete());
		Assert.assertTrue(tempDir.delete());
	}

	protected void with(Consumer<ZipStore> fn) {
		try (ZipStore zip = ZipStore.open(zipFile)) {
			fn.accept(zip);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
