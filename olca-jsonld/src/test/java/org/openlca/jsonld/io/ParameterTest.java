package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ImpactMethod;
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
		Assert.assertFalse(dao.contains(p.getRefId()));
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(p.getRefId()));
		dao.delete(p);
	}

	@Test
	public void testProcess() throws Exception {
		Process process = new Process();
		process.setRefId(UUID.randomUUID().toString());
		Parameter param = createParam(ParameterScope.PROCESS);
		process.getParameters().add(param);
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
		Process clone = dao.getForRefId(process.getRefId());
		Assert.assertEquals(param.getRefId(),
				clone.getParameters().get(0).getRefId());
	}

	@Test
	public void testImpactMethod() throws Exception {
		ImpactMethod method = new ImpactMethod();
		method.setRefId(UUID.randomUUID().toString());
		Parameter param = createParam(ParameterScope.IMPACT_METHOD);
		method.parameters.add(param);
		ImpactMethodDao dao = new ImpactMethodDao(Tests.getDb());
		dao.insert(method);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(method);
		});
		dao.delete(method);
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		ImpactMethod clone = dao.getForRefId(method.getRefId());
		Assert.assertEquals(param.getRefId(),
				clone.parameters.get(0).getRefId());
	}

	private Parameter createParam(ParameterScope scope) {
		Parameter param = new Parameter();
		param.setRefId(UUID.randomUUID().toString());
		param.setName("param");
		param.setScope(scope);
		param.setValue(42);
		param.setInputParameter(false);
		param.setFormula("21 + 21");
		return param;
	}

}
