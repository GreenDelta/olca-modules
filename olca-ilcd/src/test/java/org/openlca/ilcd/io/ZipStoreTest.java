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
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.SourceBuilder;

public class ZipStoreTest {

	private static ZipStore store;
	private static File zipFile;

	@BeforeClass
	public static void setUpStore() {
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
		DataSetInformation dataSetInfo = new DataSetInformation();
		String id = "110_abc";
		dataSetInfo.setUUID(id);
		Source source = SourceBuilder.makeSource()
				.withBaseUri("http://lca.net/ilcd")
				.withDataSetInfo(dataSetInfo).getSource();
		store.put(source, id);
		assertTrue(store.contains(Source.class, id));
		Source copy = store.get(Source.class, id);
		assertEquals(source.getSourceInformation().getDataSetInformation()
				.getUUID(), copy.getSourceInformation().getDataSetInformation()
				.getUUID());
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
		store.put(p, "abc_123");
		assertTrue(store.contains(Process.class, "abc_123"));
		Process copy = store.get(Process.class, "abc_123");
		ProductModel model = (ProductModel) copy.getProcessInformation()
				.getDataSetInformation().getOther().getAny().get(0);
		String name = model.getName();
		assertEquals("product-model-name", name);
	}

	private Process makeProductModel() {
		Process process = new Process();
		ProcessInformation pi = new ProcessInformation();
		process.setProcessInformation(pi);
		org.openlca.ilcd.processes.DataSetInformation info = new org.openlca.ilcd.processes.DataSetInformation();
		pi.setDataSetInformation(info);
		ProductModel productModel = new ProductModel();
		productModel.setName("product-model-name");
		Other other = new Other();
		info.setOther(other);
		other.getAny().add(productModel);
		return process;
	}
}
