package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;

public class ParameterDaoTest {

	@Before
	public void clearDb() {
		new ParameterDao(Tests.getDb()).deleteAll();
	}

	@Test
	public void testGetParametersOf() {
		var processDao = new ProcessDao(Tests.getDb());
		var dao = new ParameterDao(Tests.getDb());
		var proc1 = new Process();
		proc1.parameters.add(parameter("p1_1", ParameterScope.PROCESS));
		proc1.parameters.add(parameter("p1_2", ParameterScope.PROCESS));
		proc1 = processDao.insert(proc1);
		var proc2 = new Process();
		proc2.parameters.add(parameter("p2_1", ParameterScope.PROCESS));
		proc2.parameters.add(parameter("p2_2", ParameterScope.PROCESS));
		proc2 = processDao.insert(proc2);		
		dao.insert(parameter("p1", ParameterScope.GLOBAL));
		dao.insert(parameter("p2", ParameterScope.GLOBAL));
		
		var parameters = dao.getParametersOf(proc2.id);
		Assert.assertTrue(contains(parameters, "p2_1"));
		Assert.assertTrue(contains(parameters, "p2_2"));	
		Assert.assertEquals(2, parameters.size());
	}
	
	private boolean contains(List<Parameter> parameters, String name) {
		for (var param : parameters)
			if (param.scope == ParameterScope.PROCESS && name.equals(param.name))
				return true;
		return false;
	}
	
	private Parameter parameter(String name, ParameterScope scope) {
		var p = new Parameter();
		p.name = name;
		p.value = Math.random();
		if (scope == ParameterScope.GLOBAL) {
			p.refId = UUID.randomUUID().toString();
		}
		p.scope = scope;
		return p;
	}

}
