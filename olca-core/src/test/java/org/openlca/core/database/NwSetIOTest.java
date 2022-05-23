package org.openlca.core.database;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;

public class NwSetIOTest {

	private final IDatabase db = Tests.getDb();

	private final int CATEGORY_COUNT = 5;
	private final int NWSET_COUNT = 3;
	private final double FACTOR = 22d;

	private ImpactMethod method;

	@Before
	public void setUp() {
		ImpactMethod method = new ImpactMethod();
		for (int i = 0; i < NWSET_COUNT; i++) {
			NwSet set = new NwSet();
			set.name = "nwset_" + i;
			method.nwSets.add(set);
		}
		ImpactCategoryDao cdao = new ImpactCategoryDao(db);
		for (int i = 0; i < CATEGORY_COUNT; i++) {
			ImpactCategory category = new ImpactCategory();
			category.name = "category_" + i;
			category = cdao.insert(category);
			method.impactCategories.add(category);
			for (NwSet set : method.nwSets) {
				NwFactor factor = new NwFactor();
				factor.weightingFactor = FACTOR;
				factor.impactCategory = category;
				factor.normalisationFactor = FACTOR;
				set.factors.add(factor);
			}
		}
		this.method = new ImpactMethodDao(db).insert(method);
		Tests.emptyCache();
	}

	@After
	public void tearDown() {
		if (method != null)
			new ImpactMethodDao(db).delete(method);
	}

	@Test
	public void testModel() {
		ImpactMethod method = new ImpactMethodDao(db).getForId(this.method.id);
		Assert.assertEquals(CATEGORY_COUNT, method.impactCategories.size());
		Assert.assertEquals(NWSET_COUNT, method.nwSets.size());
		for(NwSet nwSet : method.nwSets) {
			Assert.assertEquals(CATEGORY_COUNT, nwSet.factors.size());
			for(NwFactor f : nwSet.factors) {
				Assert.assertEquals(f.normalisationFactor, FACTOR, 1e-20);
				Assert.assertEquals(f.weightingFactor, FACTOR, 1e-20);
				Assert.assertTrue(method.impactCategories.contains(
						f.impactCategory));
			}
		}
	}

	@Test
	public void testGetDescriptors() {
		NwSetDao dao = new NwSetDao(db);
		var all =  dao.getAll();
		var forMethod = dao.allOfMethod(method.id);
		Assert.assertEquals(NWSET_COUNT, forMethod.size());
		Assert.assertTrue(all.size() >= forMethod.size());
		Assert.assertTrue(all.containsAll(forMethod));
	}

	@Test
	public void testNwSetTable() {
		for(NwSet nwSet : method.nwSets) {
			var table = NwSetTable.of(db, nwSet);
			for(var impact : method.impactCategories) {
				Assert.assertEquals(FACTOR, table.getNormalizationFactor(
						impact.id), 1e-20);
				Assert.assertEquals(FACTOR, table.getWeightingFactor(
						impact.id), 1e-20);
			}
		}
	}
}
