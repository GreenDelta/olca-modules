package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public class UnitUseSearchTest {

	private IDatabase database = Tests.getDb();
	private UnitUseSearch search;
	private UnitGroup unitGroup;
	private Unit unit;

	@Before
	public void setup() {
		unitGroup = new UnitGroup();
		unitGroup.name = "group";
		unit = new Unit();
		unit.name = "unit";
		unitGroup.units.add(unit);
		unitGroup = new UnitGroupDao(database).insert(unitGroup);
		unit = unitGroup.getUnit(unit.name);
		search = new UnitUseSearch(database);
	}

	@After
	public void tearDown() {
		new UnitGroupDao(database).delete(unitGroup);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(unit);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInImpactCategores() {
		ImpactFactor iFactor = new ImpactFactor();
		iFactor.unit = unit;
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(iFactor);
		new ImpactCategoryDao(database).insert(category);
		List<CategorizedDescriptor> results = search.findUses(unit);
		new ImpactCategoryDao(database).delete(category);
		Descriptor expected = Descriptor.of(category);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(unit);
		new ProcessDao(database).delete(process);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.name = "process";
		Exchange exchange = new Exchange();
		exchange.unit = unit;
		process.exchanges.add(exchange);
		return new ProcessDao(database).insert(process);
	}

	@Test
	public void testFindInSocialIndicator() {
		SocialIndicator indicator = createSocialIndicator();
		List<CategorizedDescriptor> results = search.findUses(unit);
		new SocialIndicatorDao(database).delete(indicator);
		Descriptor expected = Descriptor.of(indicator);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private SocialIndicator createSocialIndicator() {
		SocialIndicator indicator = new SocialIndicator();
		indicator.name = "indicator";
		indicator.activityUnit = unit;
		return new SocialIndicatorDao(database).insert(indicator);
	}
}
