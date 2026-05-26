package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ecospold.EcoSpold;
import org.openlca.ecospold.model.DataSet;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.output.EcoSpold1Export;
import org.openlca.util.Dirs;

public class ExportConfigTest {

	private final IDatabase db = Tests.getDb();
	private File dir;

	@Before
	public void setup() throws Exception {
		dir = Files.createTempDirectory("_spold_out").toFile();
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(dir);
	}

	@Test
	public void testDefaultConfig() throws Exception {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("product", mass);
		var proc = Process.of("plain technology process", product);
		proc.documentation = new ProcessDoc();
		proc.documentation.technology = "Just a plain technology description.";
		db.insert(units, mass, product, proc);

		try (var export = EcoSpold1Export.of(db, dir).create().orElseThrow()) {
			export.export(proc);
		}

		var files = dir.listFiles((d, name) -> name.startsWith("process_"));
		assertNotNull(files);
		var spold = EcoSpold.read(files[0]).orElseThrow();
		var ds = DataSet.first(spold).orElseThrow();
		assertEquals(
			proc.documentation.technology, ds.getTechnology().getText());
	}
}
