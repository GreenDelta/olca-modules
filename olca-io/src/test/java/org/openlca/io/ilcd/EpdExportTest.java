package org.openlca.io.ilcd;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.output.Export;

public class EpdExportTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEpdExport() throws Exception {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("Product", mass);
		var actor = Actor.of("Actor");

		var impact = ImpactCategory.of("GWP", "CO2eq.");
		var result = Result.of("Some product - A1-A3", p);
		result.impactResults.add(ImpactResult.of(impact, 42));

		var epd = Epd.of("Some EPD", p);
		epd.category = CategoryDao.sync(db, ModelType.EPD, "Some", "Category");
		epd.programOperator = actor;
		epd.verifier = actor;
		epd.manufacturer = actor;
		epd.modules.add(EpdModule.of("A1-A3", result));

		db.insert(units, mass, p, actor, impact, result, epd);
		var store = new MemDataStore();
		new Export(db, store).write(epd);
		var iEpd = store.get(Process.class, epd.refId);
		assertNotNull(iEpd);


		assertEquals(p.refId, iEpd.getExchanges().get(0).getFlow().getUUID());
		assertEquals(impact.refId, iEpd.getImpactResults().get(0).getMethod().getUUID());

		store.close();

	}
}
