package org.openlca.core.matrix.cache;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Currency;
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
		BaseDao<UnitGroup> dao = new UnitGroupDao(database);
		UnitGroup group = new UnitGroup();
		group.name = "test-ug";
		Unit refUnit = new Unit();
		refUnit.name = "ref-unit";
		refUnit.conversionFactor = 1d;
		group.units.add(refUnit);
		group.referenceUnit = refUnit;
		Unit otherUnit = new Unit();
		otherUnit.name = "other-unit";
		otherUnit.conversionFactor = 42.42;
		group.units.add(otherUnit);
		dao.insert(group);
		ConversionTable table = ConversionTable.create(database);
		Assert.assertEquals(1d, table.getUnitFactor(refUnit.id), 1e-16);
		Assert.assertEquals(42.42, table.getUnitFactor(otherUnit.id), 1e-16);
		dao.delete(group);
	}

	@Test
	public void testFlowPropertyFactor() throws Exception {
		FlowDao dao = new FlowDao(database);
		Flow flow = new Flow();
		flow.name = "test-flow";
		FlowPropertyFactor factor1 = new FlowPropertyFactor();
		factor1.conversionFactor = 1d;
		flow.flowPropertyFactors.add(factor1);
		FlowPropertyFactor factor2 = new FlowPropertyFactor();
		factor2.conversionFactor = 0.42;
		flow.flowPropertyFactors.add(factor2);
		dao.insert(flow);
		ConversionTable table = ConversionTable.create(database);
		Assert.assertEquals(1d, table.getPropertyFactor(factor1.id), 1e-16);
		Assert.assertEquals(0.42, table.getPropertyFactor(factor2.id), 1e-16);
		dao.delete(flow);
	}

	@Test
	public void testGetCurrencyFactor() {
		CurrencyDao dao = new CurrencyDao(database);
		Currency eur = make("EUR", 1.0);
		Currency usd = make("USD", 0.88);
		usd.referenceCurrency = eur;
		dao.update(usd);
		ConversionTable table = ConversionTable.create(Tests.getDb());
		Assert.assertEquals(1.0, table.getCurrencyFactor(eur.id), 1e-10);
		Assert.assertEquals(0.88, table.getCurrencyFactor(usd.id), 1e-10);

		// the default factor should be 1.0
		Assert.assertEquals(1.0, table.getCurrencyFactor(-42L), 1e-10);

		dao.delete(usd);
		dao.delete(eur);
	}

	private Currency make(String code, double factor) {
		CurrencyDao dao = new CurrencyDao(database);
		Currency c = new Currency();
		c.code = code;
		c.conversionFactor = factor;
		c.referenceCurrency = c;
		return dao.insert(c);
	}
}
