package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ParameterTest extends AbstractZipTest {

	@Test
	public void testGlobal() throws Exception {
		Parameter p = createParam(ParameterScope.GLOBAL);
		ParameterDao dao = new ParameterDao(Tests.getDb());
		dao.insert(p);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(p);
		});
		dao.delete(p);
		Assert.assertFalse(dao.contains(p.refId));
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(p.refId));
		dao.delete(p);
	}

	@Test
	public void testProcess() throws Exception {
		Process process = new Process();
		process.refId = UUID.randomUUID().toString();
		Parameter param = createParam(ParameterScope.PROCESS);
		process.parameters.add(param);
		ProcessDao dao = new ProcessDao(Tests.getDb());
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
		Process clone = dao.getForRefId(process.refId);
		Assert.assertEquals(param.refId,
				clone.parameters.get(0).refId);
	}

	@Test
	public void testImpactCategory() throws Exception {
		ImpactCategory impact = new ImpactCategory();
		impact.refId = UUID.randomUUID().toString();
		Parameter param = createParam(ParameterScope.IMPACT_CATEGORY);
		impact.parameters.add(param);
		ImpactCategoryDao dao = new ImpactCategoryDao(Tests.getDb());
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

	private Parameter createParam(ParameterScope scope) {
		Parameter param = new Parameter();
		param.refId = UUID.randomUUID().toString();
		param.name = "param";
		param.scope = scope;
		param.value = 42;
		param.isInputParameter = false;
		param.formula = "21 + 21";
		return param;
	}

}
