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
import org.openlca.core.model.descriptors.NwSetDescriptor;

import java.util.List;

public class NwSetIOTest {

	private IDatabase db = Tests.getDb();

	private final int CATEGORY_COUNT = 5;
	private final int NWSET_COUNT = 3;
	private final double FACTOR = 22d;

	private ImpactMethod method;

	@Before
	public void setUp() {
		ImpactMethod method = new ImpactMethod();
		for (int i = 0; i < NWSET_COUNT; i++) {
			NwSet set = new NwSet();
			set.setName("nwset_" + i);
			method.nwSets.add(set);
		}
		for (int i = 0; i < CATEGORY_COUNT; i++) {
			ImpactCategory category = new ImpactCategory();
			category.setName("category_" + i);
			method.impactCategories.add(category);
			for (NwSet set : method.nwSets) {
				NwFactor factor = new NwFactor();
				factor.setWeightingFactor(FACTOR);
				factor.setImpactCategory(category);
				factor.setNormalisationFactor(FACTOR);
				set.factors.add(factor);
			}
		}
		this.method = db.createDao(ImpactMethod.class).insert(method);
		Tests.emptyCache();
	}

	@After
	public void tearDown() {
		if (method != null)
			db.createDao(ImpactMethod.class).delete(method);
	}

	@Test
	public void testModel() {
		ImpactMethod method = db.createDao(ImpactMethod.class)
				.getForId(this.method.getId());
		Assert.assertEquals(CATEGORY_COUNT, method.impactCategories.size());
		Assert.assertEquals(NWSET_COUNT, method.nwSets.size());
		for(NwSet nwSet : method.nwSets) {
			Assert.assertEquals(CATEGORY_COUNT, nwSet.factors.size());
			for(NwFactor f : nwSet.factors) {
				Assert.assertEquals(f.getNormalisationFactor(), FACTOR, 1e-20);
				Assert.assertEquals(f.getWeightingFactor(), FACTOR, 1e-20);
				Assert.assertTrue(method.impactCategories.contains(
						f.getImpactCategory()));
			}
		}
	}

	@Test
	public void testGetDescriptors() {
		NwSetDao dao = new NwSetDao(db);
		List<NwSetDescriptor> all =  dao.getDescriptors();
		List<NwSetDescriptor> forMethod = dao.getDescriptorsForMethod(
				method.getId());
		Assert.assertEquals(NWSET_COUNT, forMethod.size());
		Assert.assertTrue(all.size() >= forMethod.size());
		Assert.assertTrue(all.containsAll(forMethod));
	}

	@Test
	public void testNwSetTable() {
		for(NwSet nwSet : method.nwSets) {
			NwSetTable table = NwSetTable.build(db, nwSet.getId());
			for(ImpactCategory impact : method.impactCategories) {
				Assert.assertEquals(FACTOR, table.getNormalisationFactor(
						impact.getId()), 1e-20);
				Assert.assertEquals(FACTOR, table.getWeightingFactor(
						impact.getId()), 1e-20);
			}
		}
	}
}
