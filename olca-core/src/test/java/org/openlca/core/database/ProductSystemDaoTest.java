package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Scenario;

public class ProductSystemDaoTest {

	@Test
	public void testScenarions() {
		ProductSystemDao dao = new ProductSystemDao(Tests.getDb());
		ProductSystem sys = new ProductSystem();

		Scenario s1 = new Scenario();
		sys.scenarios.add(s1);
		s1.name = "Scenario 1";
		s1.isBaseline = true;
		ParameterRedef p1 = new ParameterRedef();
		p1.name = "p";
		p1.description = "just for testing";
		p1.value = 42.0;
		s1.parameters.add(p1);

		Scenario s2 = new Scenario();
		sys.scenarios.add(s2);
		s2.name = "Scenario 2";
		s2.isBaseline = false;
		s2.parameters.add(p1.clone());

		dao.insert(sys);
		sys = dao.getForId(sys.id);
		Assert.assertEquals(2, sys.scenarios.size());
		for (Scenario s : sys.scenarios) {
			if (s.isBaseline) {
				Assert.assertEquals("Scenario 1", s.name);
			} else {
				Assert.assertEquals("Scenario 2", s.name);
			}
			Assert.assertEquals("p", s.parameters.get(0).name);
		}

		dao.delete(sys);
	}

}
