package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.input.EcoSpold01Import;
import org.openlca.io.ecospold1.input.ImportConfig;
import org.openlca.io.ecospold1.output.EcoSpold1Export;

public class TestEcoSpold1IO {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProcessIO() throws Exception {

		// create the process model
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var co2 = Flow.elementary("CO2", mass);
		var steel = Flow.product("steel", mass);
		var process = Process.of("Steel production", steel);
		process.output(co2, 2);
		var entities = List.of(units, mass, co2, steel, process);
		entities.forEach(db::insert);

		var dir = Files.createTempDirectory("_spold_out");
		var export = new EcoSpold1Export(dir.toFile());
		export.export(process);
		db.clear();

		var config = new ImportConfig(db);
		var imp = new EcoSpold01Import(config);

		var files = Files.walk(dir)
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.toArray(File[]::new);

		assertNotNull(files);
		assertTrue(files.length > 0);
		imp.setFiles(files);
		imp.run();

		var processes = db.allOf(Process.class);
		assertEquals(1, processes.size());
		assertEquals("steel", process.quantitativeReference.flow.name);
		db.clear();
	}

}
