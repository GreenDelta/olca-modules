package org.openlca.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;

public class ParameterRedefSetsTest {

	private IDatabase db = Tests.getDb();

	@Before
	public void before() {
		for (var p : db.getAll(Parameter.class)) {
			db.delete(p);
		}
	}

	@Test
	// just a basic test to ensure native queries work
	public void allOf() {
		var ps = new ProductSystem();
		var p = new Parameter();
		p.name = "g1";
		p.value = 5;
		p.scope = ParameterScope.GLOBAL;
		p.isInputParameter = true;
		p = db.insert(p);
		ps = db.insert(ps);

		var all = ParameterRedefSets.allOf(db, Descriptor.of(ps));
		Assert.assertTrue(all.isBaseline);
		Assert.assertEquals(1, all.parameters.size());
		var fromDb = all.parameters.get(0);
		Assert.assertEquals("g1", fromDb.name);
		Assert.assertEquals(5d, fromDb.value, 0d);
	}

	@Test
	// just a basic test to ensure native queries work
	public void baselineOf() {
		var ps = baselinePs();
		var all = ParameterRedefSets.baselineOf(db, Descriptor.of(ps));
		Assert.assertTrue(all.isBaseline);
		Assert.assertEquals(1, all.parameters.size());
		var fromDb = all.parameters.get(0);
		Assert.assertEquals("g1", fromDb.name);
		Assert.assertEquals(7d, fromDb.value, 0d);
	}

	@Test
	// an error was thrown when two baseline sets were present, this test is to
	// ensure the fix works
	public void baselineOfWithDuplicate() {
		var ps = baselinePs();
		var set2 = new ParameterRedefSet();
		set2.isBaseline = true;
		var pr2 = new ParameterRedef();
		pr2.name = "g2";
		pr2.value = 8;
		set2.parameters.add(pr2);
		ps.parameterSets.add(set2);
		db.update(ps);
		
		var all = ParameterRedefSets.baselineOf(db, Descriptor.of(ps));
		Assert.assertTrue(all.isBaseline);
		Assert.assertEquals(1, all.parameters.size());
		var fromDb = all.parameters.get(0);
		Assert.assertEquals("g1", fromDb.name);
		Assert.assertEquals(7d, fromDb.value, 0d);
	}

	private ProductSystem baselinePs() {
		var ps = new ProductSystem();
		var p = new Parameter();
		p.name = "g1";
		p.value = 5;
		p.scope = ParameterScope.GLOBAL;
		p.isInputParameter = true;
		p = db.insert(p);
		var p2 = new Parameter();
		p2.name = "g2";
		p2.value = 6;
		p2.scope = ParameterScope.GLOBAL;
		p2.isInputParameter = true;
		p2 = db.insert(p2);
		var set = new ParameterRedefSet();
		set.isBaseline = true;
		var pr = new ParameterRedef();
		pr.name = "g1";
		pr.value = 7;
		set.parameters.add(pr);
		ps.parameterSets.add(set);
		return db.insert(ps);
	}

}
