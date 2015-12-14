package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;

public class ParameterDaoTest {

	@Before
	public void clearDb() {
		new ParameterDao(Tests.getDb()).deleteAll();
	}
	
	@Test
	public void testGetDescriptorsByNames() {
		ParameterDao dao = new ParameterDao(Tests.getDb());
		insertParameter(dao, "p1");
		insertParameter(dao, "p2");
		insertParameter(dao, "p3");
		insertParameter(dao, "p4");
		String[] names = { "p2", "p3" };
		List<ParameterDescriptor> results = dao.getDescriptors(names, ParameterScope.GLOBAL);
		Assert.assertEquals(2, results.size());
		Assert.assertNotNull(find(results, "p2"));
		Assert.assertNotNull(find(results, "p3"));
	}

	private void insertParameter(ParameterDao dao, String name) {
		Parameter p = new Parameter();
		p.setName(name);
		p.setRefId(UUID.randomUUID().toString());
		p.setScope(ParameterScope.GLOBAL);
		dao.insert(p);
	}
	
	private ParameterDescriptor find(List<ParameterDescriptor> list, String name) {
		for (ParameterDescriptor d: list)
			if (d.getName().equals(name))
				return d;
		return null;
	}

}
