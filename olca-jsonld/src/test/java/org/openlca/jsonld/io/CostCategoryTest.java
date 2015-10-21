package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.CostCategoryDao;
import org.openlca.core.model.CostCategory;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class CostCategoryTest extends AbstractZipTest {

	@Test
	public void testCostCategory() throws Exception {
		CostCategoryDao dao = new CostCategoryDao(Tests.getDb());
		CostCategory cc = createModel(dao);
		doExport(cc, dao);
		doImport(dao, cc);
		dao.delete(cc);
	}

	private CostCategory createModel(CostCategoryDao dao) {
		CostCategory cc = new CostCategory();
		cc.setName("cost category");
		cc.setRefId(UUID.randomUUID().toString());
		dao.insert(cc);
		return cc;
	}

	private void doExport(CostCategory cc, CostCategoryDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(cc);
		});
		dao.delete(cc);
		Assert.assertFalse(dao.contains(cc.getRefId()));
	}

	private void doImport(CostCategoryDao dao, CostCategory cc) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(cc.getRefId()));
		CostCategory clone = dao.getForRefId(cc.getRefId());
		Assert.assertEquals(cc.getName(), clone.getName());
	}
}
