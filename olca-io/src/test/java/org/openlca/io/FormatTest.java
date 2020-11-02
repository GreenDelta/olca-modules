package org.openlca.io;

import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;

public class FormatTest {

	@Test
	public void testDetectZOLCA() throws Exception {
		var file = Files.createTempFile("_olca_test", ".zolca").toFile();
		var format = Format.detect(file).orElseThrow();
		Assert.assertEquals(Format.ZOLCA, format);
		Assert.assertTrue(file.delete());
	}

	@Test
	public void detectES1XML() throws Exception {
		var factory = DataSetType.PROCESS.getFactory();
		var root = factory.createEcoSpold();
		var ds = factory.createDataSet();
		ds.setGenerator("openLCA tests");
		root.getDataset().add(ds);

		var file = Files.createTempFile("_olca_test", ".xml").toFile();
		EcoSpoldIO.writeTo(file, root, DataSetType.PROCESS);



	}

}
