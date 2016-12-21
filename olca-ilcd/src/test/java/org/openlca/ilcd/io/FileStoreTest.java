package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.ilcd.SampleSource;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroupBag;

public class FileStoreTest {

	private static FileStore fileStore;

	@BeforeClass
	public static void setUp() throws Exception {
		String path = System.getProperty("java.io.tmpdir");
		File rootDir = new File(path + File.separator + "itest");
		fileStore = new FileStore(rootDir);
		fileStore.prepareFolder();
	}

	@AfterClass
	public static void tearDown() {
		File file = fileStore.getRootFolder();
		deleteContent(file);
		file.delete();
	}

	private static void deleteContent(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				deleteContent(file);
			}
			file.delete();
		}
	}

	@Test
	public void testGet() throws Exception {
		UnitGroup group = fileStore.get(UnitGroup.class,
				"93a60a57-a4c8-11da-a746-0800200c9a66");
		assertNotNull(group);
		UnitGroupBag bag = new UnitGroupBag(group, "en");
		assertEquals("Units of mass", bag.getName());
	}

	@Test
	public void testPut() throws Exception {
		DataSetInfo dataSetInfo = new DataSetInfo();
		String id = UUID.randomUUID().toString();
		dataSetInfo.uuid = id;
		Source source = SampleSource.create();
		source.sourceInfo = new SourceInfo();
		source.sourceInfo.dataSetInfo = dataSetInfo;
		fileStore.put(source);
		assertTrue(fileStore.contains(Source.class, id));
	}

	@Test
	public void testDelete() throws Exception {
		String id = "5bb337b0-9a1a-11da-a72b-0800200c9a62";
		assertTrue(fileStore.contains(Contact.class, id));
		fileStore.delete(Contact.class, id);
		assertFalse(fileStore.contains(Contact.class, id));
	}

	@Test
	@Ignore
	public void testIterator() {
		fail("Not yet implemented");
	}

	@Test
	public void testContains() throws Exception {
		String id = "631b917e-eb39-4d0f-aae6-98c805513b2f";
		assertTrue(fileStore.contains(Contact.class, id));
	}

}
