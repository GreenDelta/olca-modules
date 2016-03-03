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
		param.setRefId(UUID.randomUUID().toString());
		param.setDescription("test parameter");
		param.setInputParameter(true);
		param.setScope(ParameterScope.GLOBAL);
		param.setName("p_342637");
		param.setValue(42);
		parameterDao.insert(param);
		Tests.emptyCache();
		Parameter alias = parameterDao.getForId(param.getId());
		Assert.assertEquals("p_342637", alias.getName());
		Assert.assertTrue(parameterDao.getGlobalParameters().contains(alias));
		parameterDao.delete(alias);
	}

	@Test
	public void testProcessParameters() {
		Process process = new Process();
		process.setName("test-proc");
		Parameter param = new Parameter();
		param.setRefId(UUID.randomUUID().toString());
		param.setDescription("test parameter");
		param.setInputParameter(true);
		param.setScope(ParameterScope.PROCESS);
		param.setName("p_734564");
		param.setValue(42);
		param.setUncertainty(Uncertainty.normal(42, 2));
		process.getParameters().add(param);
		ProcessDao dao = new ProcessDao(database);
		dao.insert(process);
		Tests.emptyCache();
		dao.getForId(process.getId());
		Assert.assertTrue(process.getParameters().get(0).getValue() == 42);
		Assert.assertTrue(parameterDao.getAll().contains(param));
		Assert.assertFalse(parameterDao.getGlobalParameters().contains(param));
		dao.delete(process);
		Assert.assertFalse(parameterDao.getAll().contains(param));
	}

	@Test
	public void testSystemParameterRedef() {
		ProductSystem system = new ProductSystem();
		system.setName("test system");
		ParameterRedef redef = new ParameterRedef();
		redef.setName("a");
		redef.setContextId(123L);
		redef.setValue(42);
		redef.setUncertainty(Uncertainty.normal(42, 2));
		system.getParameterRedefs().add(redef);
		ProductSystemDao dao = new ProductSystemDao(database);
		dao.insert(system);
		Tests.emptyCache();
		ProductSystem alias = dao.getForId(system.getId());
		Assert.assertTrue(
				alias.getParameterRedefs().get(0).getContextId() == 123L);
		dao.delete(system);
	}

}
