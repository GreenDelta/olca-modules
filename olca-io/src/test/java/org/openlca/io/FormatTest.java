package org.openlca.io;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.Test;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;

public class FormatTest {

	@Test
	public void testDetectZOLCA() throws Exception {
		var file = Files.createTempFile("_olca_test", ".zolca").toFile();
		var format = Format.detect(file).orElseThrow();
		assertEquals(Format.ZOLCA, format);
		Files.delete(file.toPath());
	}

	@Test
	public void testDetectES2XML() throws Exception {

		// it should recognise a *.spold file without checking the content
		var file = Files.createTempFile("_olca_test", ".spold").toFile();
		var format = Format.detect(file).orElseThrow();
		assertEquals(Format.ES2_XML, format);
		Files.delete(file.toPath());

		file = Files.createTempFile("_olca_test", ".spold").toFile();

	}

	@Test
	public void detectES1XML() throws Exception {
		var types = new DataSetType[] {
				DataSetType.PROCESS,
				DataSetType.IMPACT_METHOD,
		};

		for (var type : types) {
			var factory = type.getFactory();
			var root = factory.createEcoSpold();
			var ds = factory.createDataSet();
			ds.setGenerator("openLCA tests");
			root.getDataset().add(ds);

			var file = Files.createTempFile("_olca_test", ".xml").toFile();
			EcoSpoldIO.writeTo(file, root, type);
			var format = Format.detect(file).orElseThrow();
			assertEquals(Format.ES1_XML, format);
			Files.delete(file.toPath());
		}
	}

}
