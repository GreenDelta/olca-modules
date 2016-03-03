package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ImpactMethodTest extends AbstractZipTest {

	@Test
	public void testImpactMethod() throws Exception {
		ImpactMethodDao dao = new ImpactMethodDao(Tests.getDb());
		ImpactMethod method = createModel(dao);
		doExport(method, dao);
		doImport(dao, method);
		dao.delete(method);
	}

	private ImpactMethod createModel(ImpactMethodDao dao) {
		ImpactMethod method = new ImpactMethod();
		method.setName("method");
		method.setRefId(UUID.randomUUID().toString());
		dao.insert(method);
		return method;
	}

	private void doExport(ImpactMethod method, ImpactMethodDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(method);
		});
		dao.delete(method);
		Assert.assertFalse(dao.contains(method.getRefId()));
	}

	private void doImport(ImpactMethodDao dao, ImpactMethod method) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(method.getRefId()));
		ImpactMethod clone = dao.getForRefId(method.getRefId());
		Assert.assertEquals(method.getName(), clone.getName());
	}
}
