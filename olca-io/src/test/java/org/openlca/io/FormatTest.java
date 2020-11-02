package org.openlca.io;

import static org.junit.Assert.*;

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
		assertEquals(Format.ZOLCA, format);
		assertTrue(file.delete());
	}

	@Test
	public void testDetectES2XML() throws Exception {

		// it should recognise a *.spold file without checking the content
		var file = Files.createTempFile("_olca_test", ".spold").toFile();
		var format = Format.detect(file).orElseThrow();
		assertEquals(Format.ES2_XML, format);
		assertTrue(file.delete());

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
		var format = Format.detect(file).orElseThrow();
		assertEquals(Format.ES1_XML, format);



	}

}
