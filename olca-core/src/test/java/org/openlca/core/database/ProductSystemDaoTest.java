package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ProductSystem;

public class ProductSystemDaoTest {

	@Test
	public void testParameterRedefSets() {
		ProductSystemDao dao = new ProductSystemDao(Tests.getDb());
		ProductSystem sys = new ProductSystem();

		ParameterRedefSet s1 = new ParameterRedefSet();
		sys.parameterSets.add(s1);
		s1.name = "Baseline";
		s1.isBaseline = true;
		ParameterRedef p1 = new ParameterRedef();
		p1.name = "p";
		p1.description = "just for testing";
		p1.value = 42.0;
		s1.parameters.add(p1);

		ParameterRedefSet s2 = new ParameterRedefSet();
		sys.parameterSets.add(s2);
		s2.name = "Something else";
		s2.isBaseline = false;
		s2.parameters.add(p1.copy());

		dao.insert(sys);
		sys = dao.getForId(sys.id);
		Assert.assertEquals(2, sys.parameterSets.size());
		for (ParameterRedefSet s : sys.parameterSets) {
			if (s.isBaseline) {
				Assert.assertEquals("Baseline", s.name);
			} else {
				Assert.assertEquals("Something else", s.name);
			}
			Assert.assertEquals("p", s.parameters.get(0).name);
		}

		dao.delete(sys);
	}

}
