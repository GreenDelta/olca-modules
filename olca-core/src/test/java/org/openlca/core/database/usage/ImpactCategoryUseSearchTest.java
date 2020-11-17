package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactCategoryUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private ImpactMethod method;
	private ImpactCategory category;

	@Before
	public void setUp() {
		method = new ImpactMethod();
		new ImpactMethodDao(db).insert(method);
		category = new ImpactCategory();
		new ImpactCategoryDao(db).insert(category);
	}

	@After
	public void tearDown() {
		new ImpactMethodDao(db).delete(method);
		new ImpactCategoryDao(db).delete(category);
	}

	@Test
	public void testNoUsage() {
		assertTrue(doSearch().isEmpty());
	}

	@Test
	public void testUsage() {
		method.impactCategories.add(category);
		method = new ImpactMethodDao(db).update(method);
		List<CategorizedDescriptor> r = doSearch();
		assertEquals(1, r.size());
		assertEquals(Descriptor.of(method), r.get(0));
	}

	@Test
	public void testOneInTwo() {
		var impact = ImpactCategory.of("GWP", "CO2 eq.");
		var method1 = ImpactMethod.of("Method 1");
		method1.impactCategories.add(impact);
		var method2 = ImpactMethod.of("Method 2");
		method2.impactCategories.add(impact);
		var entities = List.of(impact, method1, method2);
		entities.forEach(db::insert);

		var results = IUseSearch.FACTORY
				.createFor(ModelType.IMPACT_CATEGORY, db)
				.findUses(Descriptor.of(impact));
		assertEquals(2, results.size());
		assertTrue(results.contains(Descriptor.of(method1)));
		assertTrue(results.contains(Descriptor.of(method2)));
		entities.forEach(db::delete);
	}

	private List<CategorizedDescriptor> doSearch() {
		ImpactDescriptor d = Descriptor.of(category);
		return IUseSearch.FACTORY.createFor(
				ModelType.IMPACT_CATEGORY, db).findUses(d);
	}
}
