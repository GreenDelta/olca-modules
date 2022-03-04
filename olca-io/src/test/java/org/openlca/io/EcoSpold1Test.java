package org.openlca.io;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.ecospold1.input.EcoSpold01Import;
import org.openlca.io.ecospold1.input.ImportConfig;
import org.openlca.io.ecospold1.output.EcoSpold1Export;
import org.openlca.util.Dirs;

public class EcoSpold1Test {

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

		io(export -> export.export(process));

		// check the import
		var processes = db.getAll(Process.class);
		assertEquals(1, processes.size());
		var p = processes.get(0);
		assertEquals(2, p.exchanges.size());
		for (var e : p.exchanges) {
			assertFalse(e.isInput);
			assertTrue(e.flow.name.equals("steel")
					|| e.flow.name.equals("CO2"));
			if (e.flow.name.equals("steel")) {
				assertEquals(e, p.quantitativeReference);
				assertEquals(1.0, e.amount, 1e-10);
			} else {
				assertEquals(2.0, e.amount, 1e-10);
			}
			assertEquals("kg", e.unit.name);
		}

		db.clear();
	}

	@Test
	public void testImpacts() {

		// create the model: two LCIA methods with
		// one LCIA category with two factors
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var ch4 = db.insert(Flow.elementary("CH4", mass));
		var gwp = ImpactCategory.of("GWP", "CO2 eq.");
		gwp.factor(co2, 1);
		gwp.factor(ch4, 24);
		db.insert(gwp);

		var method1 = ImpactMethod.of("Method 1");
		method1.impactCategories.add(gwp);
		db.insert(method1);
		var method2 = ImpactMethod.of("Method 2");
		method2.impactCategories.add(gwp);
		db.insert(method2);

		io(export -> {
			export.export(method1);
			export.export(method2);
		});

		var methods = db.getAll(ImpactMethod.class);
		assertEquals(2, methods.size());
		for (var method : methods) {
			assertTrue(method.name.equals("Method 1")
					|| method.name.equals("Method 2"));
			assertEquals(2, method.impactCategories
					.get(0).impactFactors.size());
		}

	}

	private void io(Consumer<EcoSpold1Export> fn) {
		try {
			// export it to a temporary folder
			var dir = Files.createTempDirectory("_spold_out");
			var export = new EcoSpold1Export(dir.toFile());
			fn.accept(export);

			// clear the database
			db.clear();

			// import it
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

			// delete the temporary folder
			Dirs.delete(dir);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
