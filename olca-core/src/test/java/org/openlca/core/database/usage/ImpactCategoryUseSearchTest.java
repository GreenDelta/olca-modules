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
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactCategoryUseSearchTest {

	private IDatabase db = Tests.getDb();
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

	private List<CategorizedDescriptor> doSearch() {
		ImpactCategoryDescriptor d = Descriptor.of(category);
		return IUseSearch.FACTORY.createFor(
				ModelType.IMPACT_CATEGORY, db).findUses(d);
	}
}
