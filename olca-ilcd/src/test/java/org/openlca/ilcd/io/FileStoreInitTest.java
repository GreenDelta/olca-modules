package org.openlca.ilcd.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

public class FileStoreInitTest {

	private String entityFolders[] = new String[] { "ILCD/ILCD/processes",
			"ILCD/ILCD/flows", "ILCD/ILCD/flowproperties",
			"ILCD/ILCD/unitgroups", "ILCD/ILCD/contacts", "ILCD/ILCD/sources" };

	@Test
	public void testPrepareFolder() throws Exception {
		File rootDir = setUpStore();
		testContent(rootDir);
		deleteContent(rootDir);
		assertTrue(rootDir.delete());
		assertFalse(rootDir.exists());
	}

	private File setUpStore() throws Exception {
		String path = System.getProperty("java.io.tmpdir");
		File rootDir = new File(path + File.separator + "itest_"
				+ UUID.randomUUID().toString());
		try (FileStore fileStore = new FileStore(rootDir)) {
			fileStore.prepareFolder();
		}
		return rootDir;
	}

	private void testContent(File rootDir) {
		for (String folder : entityFolders) {
			File dir = new File(rootDir, folder);
			assertTrue(dir.exists());
		}
	}

	private void deleteContent(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				deleteContent(file);
			}
			assertTrue(file.delete());
		}
	}
}
