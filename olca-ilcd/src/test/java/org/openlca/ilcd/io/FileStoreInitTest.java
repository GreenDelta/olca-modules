package org.openlca.ilcd.io;

class FileStoreInitTest {

	// private def entityFolders = [
	// "ILCD/ILCD/processes",
	// "ILCD/ILCD/flows",
	// "ILCD/ILCD/flowproperties",
	// "ILCD/ILCD/unitgroups",
	// "ILCD/ILCD/contacts",
	// "ILCD/ILCD/sources"
	// ]
	//
	// @Test
	// public void testPrepareFolder() throws Exception {
	// File rootDir = setUpStore()
	// testContent rootDir
	// deleteContent rootDir
	// assertTrue rootDir.delete()
	// assertFalse rootDir.exists()
	// }
	//
	// private File setUpStore() {
	// String path = System.getProperty("java.io.tmpdir");
	// File rootDir = new File(path + File.separator + "itest_"
	// + UUID.randomUUID().toString());
	// FileStore fileStore = new FileStore(rootDir);
	// fileStore.prepareFolder();
	// return rootDir;
	// }
	//
	// private void testContent(File rootDir) {
	// entityFolders.each {folder ->
	// File dir = new File(rootDir, folder);
	// assertTrue(dir.exists());
	// }
	// }
	//
	// private void deleteContent(File dir) {
	// for (File file : dir.listFiles()) {
	// if (file.isDirectory()) {
	// deleteContent(file);
	// }
	// assertTrue(file.delete());
	// }
	// }
}
