package org.openlca.io;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;

public class FormatTest {

	@Test
	public void testDetectByExtension() throws Exception {

		var extensions = new String[]{
				".zolca",
				".spold",
				".geojson",
				".kml",
		};

		var formats = new Format[]{
				Format.ZOLCA,
				Format.ES2_XML,
				Format.GEO_JSON,
				Format.KML,
		};

		for (int i = 0; i < extensions.length; i++) {
			var file = Files.createTempFile(
					"_olca_test", extensions[i]).toFile();
			check(file, formats[i]);
		}
	}

	@Test
	public void testDetectES2XML() throws Exception {
		var file = Files.createTempFile("_olca_test", ".xml").toFile();
		var ds = new spold2.DataSet();
		spold2.EcoSpold2.write(ds, file);
		check(file, Format.ES2_XML);
	}

	@Test
	public void testDetectES1XML() throws Exception {
		var types = new DataSetType[]{
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
			check(file, Format.ES1_XML);
		}
	}

	@Test
	public void testDetectKML() throws Exception {
		var file = Files.createTempFile("_olca_test", ".xml");
		Files.writeString(file,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\">"
				+ "<Placemark />"
				+ "</kml>");
		check(file.toFile(), Format.KML);
	}

	private void check(File file, Format expected) {
		try {
			var detected = Format.detect(file).orElseThrow();
			assertEquals(expected, detected);
			Files.delete(file.toPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
