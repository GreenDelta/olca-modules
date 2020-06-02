package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ParameterTest extends AbstractZipTest {

	@Test
	public void testGlobal() {
		var param = Parameter.global("param", "1+1");
		var dao = new ParameterDao(Tests.getDb());
		dao.insert(param);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(param);
		});
		dao.delete(param);
		Assert.assertFalse(dao.contains(param.refId));
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(param.refId));
		dao.delete(param);
	}

	@Test
	public void testProcess() {
		var process = new Process();
		process.refId = UUID.randomUUID().toString();
		var param = process.parameter("param", 42);
		var dao = new ProcessDao(Tests.getDb());
		dao.insert(process);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(process);
		});
		dao.delete(process);
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		var clone = dao.getForRefId(process.refId);
		Assert.assertEquals(param.refId,
				clone.parameters.get(0).refId);
	}

	@Test
	public void testImpactCategory() {
		var impact = new ImpactCategory();
		impact.refId = UUID.randomUUID().toString();
		var param = impact.parameter("param", 42);
		var dao = new ImpactCategoryDao(Tests.getDb());
		dao.insert(impact);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(impact);
		});
		dao.delete(impact);
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		ImpactCategory clone = dao.getForRefId(impact.refId);
		Assert.assertEquals(param.refId,
				clone.parameters.get(0).refId);
	}
}
