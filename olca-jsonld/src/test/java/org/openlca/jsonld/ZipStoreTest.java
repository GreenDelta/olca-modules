package org.openlca.jsonld;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;

public class ZipStoreTest {

	private File tempDir;
	private File zipFile;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("olca-json_").toFile();
		zipFile = new File(tempDir, "my zip.zip");
	}

	@After
	public void tearDown() throws Exception {
		Assert.assertTrue(zipFile.delete());
		Assert.assertTrue(tempDir.delete());
	}

	@Test
	public void createEntry() throws Exception {
		ZipStore store = ZipStore.open(zipFile);
		JsonObject json = new JsonObject();
		json.addProperty("@id", "abc");
		store.add(ModelType.ACTOR, "abc", json);
		store.close();
		store = ZipStore.open(zipFile);
		Assert.assertTrue(store.contains(ModelType.ACTOR, "abc"));
		store.close();
	}

}
