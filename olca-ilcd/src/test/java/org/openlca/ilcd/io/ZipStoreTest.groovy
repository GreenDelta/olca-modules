package org.openlca.ilcd.file.io;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.SourceBuilder;

class ZipStoreTest {

	static ZipStore store
	static def zipFile

	@BeforeClass
	static void setUpStore() {
		def tempDir = System.getProperty("java.io.tmpdir");
		def path = "$tempDir\\test_${UUID.randomUUID()}.zip"
		zipFile = new File(path)
		store = new ZipStore(zipFile)
	}

	@AfterClass
	static void tearDown() {
		store.close()
		def deleted = zipFile.delete()
		println "zip file $zipFile deleted = $deleted"
	}

	@Test
	void testWithSource() {
		DataSetInformation dataSetInfo = new DataSetInformation()
		String id = "110_abc"
		dataSetInfo.setUUID(id)
		Source source = SourceBuilder.makeSource()
				.withBaseUri("http://lca.net/ilcd")
				.withDataSetInfo(dataSetInfo).getSource()
		store.put(source, id)
		assertTrue store.contains(Source.class, id)
		def copy = store.get(Source.class, id)
		def uuid = {s -> s.sourceInformation.dataSetInformation.uuid}
		assertEquals uuid(source), uuid(copy)
		assertNotNull store.iterator(Source.class).next()
	}

	@Test
	void testNoContact() {
		assertFalse store.contains(Contact.class, "110_abc")
		assertFalse store.iterator(Contact.class).hasNext()
	}

	@Test
	void testWithProductModel() {
		Process p = makeProductModel()
		store.put(p, "abc_123")
		assertTrue store.contains(Process.class, "abc_123")
		def copy = store.get(Process.class, "abc_123")
		def name = copy.processInformation.dataSetInformation.other.any[0].name
		assertEquals "product-model-name", name
	}

	private Process makeProductModel() {
		Process process = new Process();
		def pi = new ProcessInformation();
		process.setProcessInformation(pi);
		def info = new org.openlca.ilcd.processes.DataSetInformation();
		pi.setDataSetInformation(info);
		ProductModel productModel = new ProductModel();
		productModel.setName("product-model-name");
		Other other = new Other();
		info.setOther(other);
		other.getAny().add(productModel);
		return process
	}
}
