package org.openlca.core.matrix.cache;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ConversionTableTest {

	private IDatabase database = Tests.getDb();

	@Test
	public void testDefaultFactors() {
		// the default factor for unknown units and flow properties should be 1
		ConversionTable table = ConversionTable.create(database);
		Assert.assertEquals(1d, table.getUnitFactor(999999999999L), 1e-16);
		Assert.assertEquals(1d, table.getPropertyFactor(999999999999L), 1e-16);
	}

	@Test
	public void testUnitFactor() throws Exception {
		BaseDao<UnitGroup> dao = database.createDao(UnitGroup.class);
		UnitGroup group = new UnitGroup();
		group.setName("test-ug");
		Unit refUnit = new Unit();
		refUnit.setName("ref-unit");
		refUnit.setConversionFactor(1d);
		group.getUnits().add(refUnit);
		group.setReferenceUnit(refUnit);
		Unit otherUnit = new Unit();
		otherUnit.setName("other-unit");
		otherUnit.setConversionFactor(42.42);
		group.getUnits().add(otherUnit);
		dao.insert(group);
		ConversionTable table = ConversionTable.create(database);
		Assert.assertEquals(1d, table.getUnitFactor(refUnit.getId()), 1e-16);
		Assert.assertEquals(42.42, table.getUnitFactor(otherUnit.getId()), 1e-16);
		dao.delete(group);
	}

	@Test
	public void testFlowPropertyFactor() throws Exception {
		FlowDao dao = new FlowDao(database);
		Flow flow = new Flow();
		flow.setName("test-flow");
		FlowPropertyFactor factor1 = new FlowPropertyFactor();
		factor1.setConversionFactor(1d);
		flow.getFlowPropertyFactors().add(factor1);
		FlowPropertyFactor factor2 = new FlowPropertyFactor();
		factor2.setConversionFactor(0.42);
		flow.getFlowPropertyFactors().add(factor2);
		dao.insert(flow);
		ConversionTable table = ConversionTable.create(database);
		Assert.assertEquals(1d, table.getPropertyFactor(factor1.getId()), 1e-16);
		Assert.assertEquals(0.42, table.getPropertyFactor(factor2.getId()), 1e-16);
		dao.delete(flow);
	}
}
