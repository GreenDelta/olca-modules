package org.openlca.core.database.usage;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;

import com.google.common.base.Objects;

public class UsedParametersTest {

	private IDatabase db = Tests.getEmptyDb();

	@Test
	public void testInSystem() {
		parameter(ParameterScope.GLOBAL, "g1", 5);
		parameter(ParameterScope.GLOBAL, "g2", 6);
		parameter(ParameterScope.GLOBAL, "g3", 7);
		parameter(ParameterScope.GLOBAL, "g4", 8);
		parameter(ParameterScope.GLOBAL, "g5", "3*g4");
		parameter(ParameterScope.GLOBAL, "p1", 1);
		parameter(ParameterScope.GLOBAL, "g6", 1);

		var p1 = new Process();
		var e1 = new Exchange();
		e1.formula = "g1";
		p1.exchanges.add(e1);
		p1 = db.insert(p1);

		var p2 = new Process();
		var e2 = new Exchange();
		e2.formula = "g5";
		p2.exchanges.add(e2);
		p2 = db.insert(p2);

		var ps1 = new ProductSystem();
		ps1.processes.add(p1.id);
		ps1.processes.add(p2.id);
		ps1 = db.insert(ps1);

		var p3 = new Process();
		p3.parameters.add(parameter(ParameterScope.PROCESS, "p1", 4));
		p3.parameters.add(parameter(ParameterScope.PROCESS, "p2", 3));
		var e3 = new Exchange();
		e3.formula = "g2*p1*g6";
		p3.exchanges.add(e3);
		p3 = db.insert(p3);

		var p4 = new Process();
		p4.parameters.add(parameter(ParameterScope.PROCESS, "p1", 3));
		p4.parameters.add(parameter(ParameterScope.PROCESS, "p2", 5));
		p4.parameters.add(parameter(ParameterScope.PROCESS, "p3", 1));
		p4.parameters.add(parameter(ParameterScope.PROCESS, "g6", 1));
		var e4 = new Exchange();
		e4.formula = "g3";
		p4.exchanges.add(e4);
		var a1 = new AllocationFactor();
		a1.formula = "p1*p2*g6";
		p4.allocationFactors.add(a1);
		p4 = db.insert(p4);

		var ps2 = new ProductSystem();
		ps2.processes.add(p3.id);
		ps2.processes.add(p4.id);
		ps2 = db.insert(ps2);

		var used = UsedParameters.ofSystem(db, Descriptor.of(ps1));
		Assert.assertTrue(contains(used, "g1"));
		Assert.assertTrue(contains(used, "g4"));
		Assert.assertEquals(2, used.size());

		used = UsedParameters.ofSystem(db, Descriptor.of(ps2));
		Assert.assertTrue(contains(used, "g2"));
		Assert.assertTrue(contains(used, "g3"));
		Assert.assertTrue(contains(used, "g6"));
		Assert.assertTrue(contains(used, "p1", ModelType.PROCESS, p3.id));
		Assert.assertTrue(contains(used, "p1", ModelType.PROCESS, p4.id));
		Assert.assertTrue(contains(used, "p2", ModelType.PROCESS, p4.id));
		Assert.assertTrue(contains(used, "g6", ModelType.PROCESS, p4.id));
		Assert.assertEquals(7, used.size());
	}

	private boolean contains(
			List<ParameterRedef> redefs,
			String name) {
		return contains(redefs, name, null, null);
	}

	private boolean contains(
			List<ParameterRedef> redefs,
			String name,
			ModelType contextType,
			Long contextId) {
		for (var redef : redefs) {
			if (!redef.name.equals(name))
				continue;
			if (redef.contextType != contextType)
				continue;
			if (!Objects.equal(redef.contextId, contextId))
				continue;
			return true;
		}
		return false;
	}

	private Parameter parameter(
			ParameterScope scope,
			String name,
			double value) {
		var p = new Parameter();
		p.name = name;
		p.value = value;
		p.scope = scope;
		p.isInputParameter = true;
		if (scope != ParameterScope.GLOBAL)
			return p;
		p.refId = UUID.randomUUID().toString();
		return db.insert(p);
	}

	private Parameter parameter(
			ParameterScope scope,
			String name,
			String formula) {
		var p = new Parameter();
		p.name = name;
		p.formula = formula;
		p.scope = scope;
		if (scope != ParameterScope.GLOBAL)
			return p;
		p.refId = UUID.randomUUID().toString();
		return db.insert(p);
	}

}
