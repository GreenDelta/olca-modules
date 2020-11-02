package org.openlca.io;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;
import org.openlca.io.ilcd.ILCDExport;
import org.openlca.io.ilcd.output.ExportConfig;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;

public class FormatTest {

	@Test
	public void testDetectByExtension() throws Exception {

		var extensions = new String[]{
				".zolca",
				".spold",
				".geojson",
				".kml",
				".xlsx",
		};

		var formats = new Format[]{
				Format.ZOLCA,
				Format.ES2_XML,
				Format.GEO_JSON,
				Format.KML,
				Format.EXCEL,
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

	@Test
	public void testDetectJSONLD() throws Exception {
		var db = Tests.getDb();
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var process = db.insert(Process.of("Steel production", steel));

		var file = Files.createTempFile("_olca_test", ".zip").toFile();
		Files.delete(file.toPath());
		try (var zip = ZipStore.open(file)) {
			new JsonExport(db, zip).write(process);
		}
		check(file, Format.JSON_LD_ZIP);
		Tests.clearDb();
	}

	@Test
	public void testDetectILCD() throws Exception {
		var db = Tests.getDb();
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var process = db.insert(Process.of("Steel production", steel));

		var file = Files.createTempFile("_olca_test", ".zip").toFile();
		Files.delete(file.toPath());
		try (var zip = new org.openlca.ilcd.io.ZipStore(file)) {
			var export = new ILCDExport(new ExportConfig(db, zip));
			export.export(process);
			export.close();
		}
		check(file, Format.ILCD_ZIP);
		Tests.clearDb();
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
