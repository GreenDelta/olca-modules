package org.openlca.io.ilcd;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.doc.Completeness;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.input.Import;
import org.openlca.io.ilcd.output.Export;

import static org.junit.Assert.*;

public class CompletenessTest {

	private final IDatabase db = Tests.getDb();
	private final String allQuantified =
			FlowCompleteness.ALL_RELEVANT_FLOWS_QUANTIFIED.value();

	@Test
	public void testCompleteness() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		db.insert(units, mass, p);

		var process = Process.of("P", p);
		var completeness = new Completeness();
		completeness.put("Product model", allQuantified);
		for (var impact : ImpactCategory.values()) {
			completeness.put(impact.value(), allQuantified);
		}
		completeness.writeTo(process);

		var store = new MemDataStore();
		new Export(db, store).write(process);
		Import.of(store, db).run();
		process = db.get(Process.class, process.refId);
		// var ds = store.get(org.openlca.ilcd.processes.Process.class, process.refId);
		// System.out.println(Xml.toString(ds));

		assertNotNull(process);
		completeness = Completeness.readFrom(process);
		assertEquals(allQuantified, completeness.get("Product model"));
		for (var impact : ImpactCategory.values()) {
			assertEquals(allQuantified, completeness.get(impact.value()));
		}
	}
}
