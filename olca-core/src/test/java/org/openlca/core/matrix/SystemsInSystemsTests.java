package org.openlca.core.matrix;

import java.util.UUID;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class SystemsInSystemsTests {

	@Test
	public void testCalc() {
		IDatabase db = Tests.getDb();
		UnitGroupDao ugDao = new UnitGroupDao(db);
		FlowPropertyDao fpDao = new FlowPropertyDao(db);
		FlowDao flowDao = new FlowDao(db);
		ProcessDao processDao = new ProcessDao(db);
		ProductSystemDao systemDao = new ProductSystemDao(db);

		UnitGroup ug6939 = new UnitGroup();
		ug6939.setRefId(UUID.randomUUID().toString());
		ug6939.setName("Units of mass");
		Unit kg = new Unit();
		kg.setName("kg");
		kg.setConversionFactor(1.0);
		kg.setRefId(UUID.randomUUID().toString());

	}

}
