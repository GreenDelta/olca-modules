package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;

public class ImpactCategoryUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private ImpactMethod method;
	private ImpactCategory impact;

	@Before
	public void setUp() {
		method = db.insert(new ImpactMethod());
		impact = db.insert(new  ImpactCategory());
	}

	@After
	public void tearDown() {
		db.delete(method, impact);
	}

	@Test
	public void testNoUsage() {
		var r = UsageSearch.of(ModelType.IMPACT_CATEGORY, db).find(impact.id);
		assertTrue(r.isEmpty());
	}

	@Test
	public void testUsage() {
		method.impactCategories.add(impact);
		method = db.update(method);
		var r = UsageSearch.of(ModelType.IMPACT_CATEGORY, db).find(impact.id);
		assertEquals(1, r.size());
		assertEquals(Descriptor.of(method), r.iterator().next());
	}

	@Test
	public void testOneInTwo() {
		var method1 = ImpactMethod.of("Method 1");
		method1.impactCategories.add(impact);
		var method2 = ImpactMethod.of("Method 2");
		method2.impactCategories.add(impact);
		db.insert(method1, method2);

		var r = UsageSearch.of(ModelType.IMPACT_CATEGORY, db).find(impact.id);
		assertEquals(2, r.size());
		assertTrue(r.contains(Descriptor.of(method1)));
		assertTrue(r.contains(Descriptor.of(method2)));
		db.delete(method1, method2);
	}

}
