package org.openlca.io.xls;

import java.nio.file.Files;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.io.Tests;
import org.openlca.io.xls.process.input.ExcelImport;
import org.openlca.io.xls.process.output.ExcelExport;

public class ProcessIOTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleProcess() throws Exception {

		// create a simple process
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var co2 = db.insert(Flow.elementary("C02", mass));
		var process = Process.of("Steel production", steel);
		process.output(co2, 2);
		process = db.insert(process);

		// export and delete it
		var file = Files.createTempFile("_olca_test_", ".xlsx").toFile();
		new ExcelExport(file, db, List.of(Descriptor.of(process))).run();
		db.delete(process);

		// import and check it
		new ExcelImport(file, db).run();
		var clone = db.get(Process.class, process.refId);
		Assert.assertNotEquals(process.id, clone.id);
		Assert.assertEquals(2, process.exchanges.size());

		// clean up
		Files.delete(file.toPath());
		db.clear();
	}
}
