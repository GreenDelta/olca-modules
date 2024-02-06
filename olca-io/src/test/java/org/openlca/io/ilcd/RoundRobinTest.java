package org.openlca.io.ilcd;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.input.Import;
import org.openlca.io.ilcd.output.Export;

import java.util.List;
import java.util.UUID;

public class RoundRobinTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEmptyDataSets() throws Exception {
		var list = List.of(
				new UnitGroup(),
				new FlowProperty(),
				new Flow(),
				new Process(),
				new ImpactCategory(),
				new Epd(),
				new Actor(),
				new Source(),
				new ProductSystem());

		var store = new MemDataStore();
		for (var e : list) {
			e.refId = UUID.randomUUID().toString();
			new Export(db, store).write(e);
			db.delete(e);
		}

		Import.of(store, db)
				.withAllFlows(true)
				.run();
		for (var e : list) {
			var copy = db.get(e.getClass(), e.refId);
			assertNotNull(e.getClass() + " missing", copy);
			assertEquals(e.refId, copy.refId);
			db.delete(copy);
		}
		store.close();
	}

	@Test
	public void testBasicModel() throws Exception {

		// create model
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var e = Flow.elementary("e", mass);
		var process = Process.of("P", p);
		process.output(e, 21);
		var impact = ImpactCategory.of("I", "eq");
		impact.factor(e, 2);

		var system = ProductSystem.of("S", process);
		var result = Result.of("R", p);
		result.flowResults.add(FlowResult.outputOf(e, 21));
		result.impactResults.add(ImpactResult.of(impact, 42));
		var epd = Epd.of("EPD", p);
		epd.modules.add(EpdModule.of("A1", result));
		db.insert(units, mass, p, e, process, impact, system, result, epd);

		// export model and cleanup db
		var store = new MemDataStore();
		var export = new Export(db, store);
		export.write(system);
		export.write(epd);
		var all = new RootEntity[]{
				epd, result, system, impact, process, e, p, mass, units};
		checkPresent(true, all);
		db.delete(epd, result, system, impact, process, e, p, mass, units);
		checkPresent(false, all);

		// import
		Import.of(store, db).run();
		checkPresent(true, all);

		store.close();
	}

	private void checkPresent(boolean b, RootEntity... es) {
		for (var e : es) {
			var x = db.get(e.getClass(), e.refId);
			if (!b) {
				assertNull(x);
			} else {
				assertNotNull(x);
				assertEquals(e.refId, x.refId);
				assertEquals(e.name, x.name);
			}
		}
	}

}
