package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.ilcd.SampleSource;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;

public class ZipStoreTest {

	private static ZipStore store;
	private static File zipFile;

	@BeforeClass
	public static void setUpStore() throws Exception {
		String tempDir = System.getProperty("java.io.tmpdir");
		String path = tempDir + File.separator + "test_" + UUID.randomUUID()
				+ ".zip";
		zipFile = new File(path);
		store = new ZipStore(zipFile);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		store.close();
		boolean deleted = zipFile.delete();
		System.out.println("zip file " + zipFile + "  deleted = " + deleted);
	}

	@Test
	public void testWithSource() throws Exception {
		DataSetInfo info = new DataSetInfo();
		info.uuid = UUID.randomUUID().toString();
		Source source = SampleSource.create();
		source.sourceInfo = new SourceInfo();
		source.sourceInfo.dataSetInfo = info;
		store.put(source);
		assertTrue(store.contains(Source.class, source.getUUID()));
		Source copy = store.get(Source.class, source.getUUID());
		assertEquals(source.sourceInfo.dataSetInfo.uuid, copy.sourceInfo.dataSetInfo.uuid);
		assertNotNull(store.iterator(Source.class).next());
	}

	@Test
	public void testNoContact() throws Exception {
		assertFalse(store.contains(Contact.class, "110_abc"));
		assertFalse(store.iterator(Contact.class).hasNext());
	}

	@Test
	public void testWithProductModel() throws Exception {
		Process p = makeProductModel();
		store.put(p);
		assertTrue(store.contains(Process.class, p.getUUID()));
		Process copy = store.get(Process.class, p.getUUID());
		ProductModel model = (ProductModel) copy.processInfo.dataSetInfo.other.any.get(0);
		String name = model.getName();
		assertEquals("product-model-name", name);
	}

	private Process makeProductModel() {
		Process process = new Process();
		ProcessInfo pi = new ProcessInfo();
		process.processInfo = pi;
		org.openlca.ilcd.processes.DataSetInfo info = new org.openlca.ilcd.processes.DataSetInfo();
		pi.dataSetInfo = info;
		info.uuid = UUID.randomUUID().toString();
		ProductModel productModel = new ProductModel();
		productModel.setName("product-model-name");
		Other other = new Other();
		info.other = other;
		other.any.add(productModel);
		return process;
	}
}
