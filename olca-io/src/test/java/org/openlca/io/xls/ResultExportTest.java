package org.openlca.io.xls;

import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Tests;
import org.openlca.io.xls.results.system.ResultExport;

public class ResultExportTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleResult() throws Exception {
		// create a simple process
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var co2 = db.insert(Flow.elementary("C02", mass));
		var process = Process.of("Steel production", steel);
		process.output(co2, 2);
		process = db.insert(process);

		// create a system and calculate it
		var sys = ProductSystem.of(process);
		var setup = CalculationSetup.fullAnalysis(sys);
		var calculator = new SystemCalculator(db);
		var result = calculator.calculateFull(setup);

		// run the export
		var file = Files.createTempFile("_olca_test_", ".xlsx").toFile();
		new ResultExport(setup, result, file, EntityCache.create(db)).run();
		Assert.assertTrue(file.length() > 0);

		// clean up
		Files.delete(file.toPath());
		db.clear();
	}
}
