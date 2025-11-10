package org.openlca.io.simapro.csv;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.io.Tests;
import org.openlca.io.simapro.csv.input.SimaProCsvImport;
import org.openlca.io.simapro.csv.output.SimaProExport;

public class UncertaintyTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testLogNormal() {
		with(Uncertainty.logNormal(42, 1.25), u -> {
			assertEquals(u.distributionType, UncertaintyType.LOG_NORMAL);
			assertEquals(u.parameter1, 42.0, 1e-10);
			assertEquals(u.parameter2, 1.25, 1e-10);
		});
	}

	@Test
	public void testNormal() {
		with(Uncertainty.normal(42, 4.2), u -> {
			assertEquals(u.distributionType, UncertaintyType.NORMAL);
			assertEquals(u.parameter1, 42.0, 1e-10);
			assertEquals(u.parameter2, 4.2, 1e-10);
		});
	}

	private void with(Uncertainty u, Consumer<Uncertainty> fn) {

		// create the test process
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var air = Category.of("air", ModelType.FLOW);
		var p = Flow.product("p #sp-unc-test", mass);
		var e = Flow.elementary("e", mass);
		e.category = air;
		var process = Process.of("P #sp-unc-test", p);
		process.output(e, 42).uncertainty = u;
		db.insert(units, mass, air, p, e, process);

		try {
			// write the process to a file
			var temp = Files.createTempFile("sp-test", ".csv");
			SimaProExport.of(db, List.of(Descriptor.of(process)))
					.writeTo(temp.toFile());

			// delete the original process and import the file
			db.delete(process);
			new SimaProCsvImport(db, temp.toFile()).run();
			Files.delete(temp);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		// find the process
		var d = db.getDescriptors(Process.class)
				.stream()
				.filter(di -> di.name.contains("#sp-unc-test"))
				.findAny()
				.orElseThrow();
		process = db.get(Process.class, d.id);

		// find the uncertainty & run tests
		var ui = process.exchanges.stream()
				.filter(ei -> ei.flow.name.equals("e"))
				.findAny()
				.orElseThrow()
				.uncertainty;
		fn.accept(ui);

		// clean up
		db.clear();
	}
}
