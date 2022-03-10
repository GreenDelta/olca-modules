package org.openlca.core.database.usage;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class UnitUsageSearchTest {

	private final IDatabase db = Tests.getDb();
	private UnitUsageSearch search;
	private UnitGroup group;
	private Unit unit;

	@Before
	public void setup() {
		group = db.insert(UnitGroup.of("Units of mass", "kg"));
		unit = group.referenceUnit;
		search = new UnitUsageSearch(db, unit);
	}

	@After
	public void tearDown() {
		db.delete(group);
	}

	@Test
	public void testFindNoUsage() {
		var models = search.run();
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInImpactCategories() {
		var impact = ImpactCategory.of("my impact");
		var factor = new ImpactFactor();
		factor.unit = unit;
		impact.impactFactors.add(factor);
		check(impact);
	}

	@Test
	public void testFindInProcesses() {
		var process = new Process();
		var exchange = new Exchange();
		exchange.unit = unit;
		process.exchanges.add(exchange);
		check(process);
	}

	@Test
	public void testFindInResult() {
		var result = new Result();
		var flowResult = new FlowResult();
		flowResult.unit = unit;
		result.flowResults.add(flowResult);
		check(result);
	}

	@Test
	public void testFindInSocialIndicator() {
		var indicator = new SocialIndicator();
		indicator.name = "indicator";
		indicator.activityUnit = unit;
		check(indicator);
	}

	@Test
	public void testFindInProject() {
		var project = new Project();
		var variant = new ProjectVariant();
		variant.unit = unit;
		project.variants.add(variant);
		check(project);
	}

	private void check(RootEntity e) {
		db.insert(e);
		var results = search.run();
		db.delete(e);
		var expected = Descriptor.of(e);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

}
