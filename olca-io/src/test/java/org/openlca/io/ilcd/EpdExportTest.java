package org.openlca.io.ilcd;

import static org.junit.Assert.*;

import jakarta.xml.bind.JAXB;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.ilcd.io.XmlBinder;
import org.openlca.ilcd.processes.Process;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.output.Export;

import java.io.StringWriter;

public class EpdExportTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEpdExport() throws Exception {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("Product", mass);
		var epd = Epd.of("Some EPD", p);
		epd.category = CategoryDao.sync(db, ModelType.EPD, "Some", "Category");
		db.insert(units, mass, p, epd);

		var store = new MemDataStore();
		new Export(db, store).write(epd);
		var iEpd = store.get(Process.class, epd.refId);
		assertNotNull(iEpd);

		var w = new StringWriter();
		new XmlBinder().toWriter(iEpd, w);
		System.out.println(w);

		store.close();

	}

}
