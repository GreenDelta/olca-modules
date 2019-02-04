package org.openlca.core.database;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;

public class ParameterIOTests {

	private IDatabase database = Tests.getDb();
	private ParameterDao parameterDao = new ParameterDao(database);

	@Test
	public void testGlobalParameters() {
		Parameter param = new Parameter();
		param.refId = UUID.randomUUID().toString();
		param.description = "test parameter";
		param.isInputParameter = true;
		param.scope = ParameterScope.GLOBAL;
		param.name = "p_342637";
		param.value = (double) 42;
		parameterDao.insert(param);
		Tests.emptyCache();
		Parameter alias = parameterDao.getForId(param.id);
		Assert.assertEquals("p_342637", alias.name);
		Assert.assertTrue(parameterDao.getGlobalParameters().contains(alias));
		parameterDao.delete(alias);
	}

	@Test
	public void testProcessParameters() {
		Process process = new Process();
		process.name = "test-proc";
		Parameter param = new Parameter();
		param.refId = UUID.randomUUID().toString();
		param.description = "test parameter";
		param.isInputParameter = true;
		param.scope = ParameterScope.PROCESS;
		param.name = "p_734564";
		param.value = (double) 42;
		param.uncertainty = Uncertainty.normal(42, 2);
		process.parameters.add(param);
		ProcessDao dao = new ProcessDao(database);
		dao.insert(process);
		Tests.emptyCache();
		dao.getForId(process.id);
		Assert.assertTrue(process.parameters.get(0).value == 42);
		Assert.assertTrue(parameterDao.getAll().contains(param));
		Assert.assertFalse(parameterDao.getGlobalParameters().contains(param));
		dao.delete(process);
		Assert.assertFalse(parameterDao.getAll().contains(param));
	}

	@Test
	public void testSystemParameterRedef() {
		ProductSystem system = new ProductSystem();
		system.name = "test system";
		ParameterRedef redef = new ParameterRedef();
		redef.name = "a";
		redef.contextId = 123L;
		redef.value = (double) 42;
		redef.uncertainty = Uncertainty.normal(42, 2);
		system.parameterRedefs.add(redef);
		ProductSystemDao dao = new ProductSystemDao(database);
		dao.insert(system);
		Tests.emptyCache();
		ProductSystem alias = dao.getForId(system.id);
		Assert.assertTrue(
				alias.parameterRedefs.get(0).contextId == 123L);
		dao.delete(system);
	}

}
