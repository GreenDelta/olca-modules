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

	private void insertParameter(ParameterDao dao, String name) {
		Parameter p = new Parameter();
		p.name = name;
		p.refId = UUID.randomUUID().toString();
		p.scope = ParameterScope.GLOBAL;
		dao.insert(p);
	}

	private ParameterDescriptor find(List<ParameterDescriptor> list, String name) {
		for (ParameterDescriptor d: list)
			if (d.name.equals(name))
				return d;
		return null;
	}

}
