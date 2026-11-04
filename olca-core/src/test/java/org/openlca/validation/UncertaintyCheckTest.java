package org.openlca.validation;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.validation.Item.Type;

public class UncertaintyCheckTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void setup() {
		db.clear();
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testValidateLogNormal() {
		var t = UncertaintyType.LOG_NORMAL;
		assertNull(UncertaintyCheck.validate(t, 2.0, 1.0, null));
		assertNull(UncertaintyCheck.validate(t, -2.0, 1.5, null));
		assertNull(UncertaintyCheck.validate(t, 0.0, 1.5, null));
		assertNotNull(UncertaintyCheck.validate(t, 2.0, 0.0, null));
		assertNotNull(UncertaintyCheck.validate(t, 2.0, 0.5, null));
		assertNotNull(UncertaintyCheck.validate(t, 2.0, -2.0, null));
		assertNotNull(UncertaintyCheck.validate(t, 2.0, null, null));
		assertNotNull(UncertaintyCheck.validate(t, 2.0, Double.NaN, null));
	}

	@Test
	public void testValidateOthers() {
		assertNull(UncertaintyCheck.validate(UncertaintyType.NONE, null, null, null));

		var n = UncertaintyType.NORMAL;
		assertNull(UncertaintyCheck.validate(n, 2.0, 0.0, null));
		assertNotNull(UncertaintyCheck.validate(n, 2.0, -1.0, null));

		var u = UncertaintyType.UNIFORM;
		assertNull(UncertaintyCheck.validate(u, 1.0, 1.0, null));
		assertNotNull(UncertaintyCheck.validate(u, 2.0, 1.0, null));

		var tri = UncertaintyType.TRIANGLE;
		assertNull(UncertaintyCheck.validate(tri, 1.0, 2.0, 3.0));
		assertNotNull(UncertaintyCheck.validate(tri, 1.0, 4.0, 3.0));
		assertNotNull(UncertaintyCheck.validate(tri, 1.0, 2.0, null));
	}

	@Test
	public void testExchange() {
		var mass = mass();
		var p = db.insert(Flow.product("p", mass));
		var e = db.insert(Flow.elementary("e", mass));
		var process = Process.of("P", p);
		process.output(e, 2).uncertainty = Uncertainty.logNormal(2, 0);
		db.insert(process);
		assertWarning(ModelType.PROCESS, process.id);
	}

	@Test
	public void testImpactFactor() {
		var e = db.insert(Flow.elementary("e", mass()));
		var impact = ImpactCategory.of("I");
		impact.factor(e, 1).uncertainty = Uncertainty.logNormal(1, 0.5);
		db.insert(impact);
		assertWarning(ModelType.IMPACT_CATEGORY, impact.id);
	}

	@Test
	public void testGlobalParameter() {
		var param = Parameter.global("g", 2);
		param.uncertainty = Uncertainty.logNormal(2, 0);
		db.insert(param);
		assertWarning(ModelType.PARAMETER, param.id);
	}

	@Test
	public void testProcessParameter() {
		var p = db.insert(Flow.product("p", mass()));
		var process = Process.of("P", p);
		var param = Parameter.process("x", 2);
		param.uncertainty = Uncertainty.normal(2, -1);
		process.parameters.add(param);
		db.insert(process);
		assertWarning(ModelType.PROCESS, process.id);
	}

	@Test
	public void testParameterRedef() {
		var param = db.insert(Parameter.global("g", 2));
		var p = db.insert(Flow.product("p", mass()));
		var process = db.insert(Process.of("P", p));
		var system = ProductSystem.of(process);
		var redef = ParameterRedef.of(param, 2);
		redef.uncertainty = Uncertainty.logNormal(2, 0);
		system.parameterSets.add(ParameterRedefSet.of("baseline", redef));
		db.insert(system);
		assertWarning(ModelType.PRODUCT_SYSTEM, system.id);
	}

	@Test
	public void testValidDataHasNoWarnings() {
		var mass = mass();
		var p = db.insert(Flow.product("p", mass));
		var e = db.insert(Flow.elementary("e", mass));
		var process = Process.of("P", p);
		process.output(e, 2).uncertainty = Uncertainty.logNormal(2, 1.5);
		db.insert(process);
		var v = validate();
		for (var item : v.items()) {
			assertTrue("unexpected item: " + item.message,
					item.type != Type.WARNING
							|| !item.message.contains("distribution"));
		}
	}

	private FlowProperty mass() {
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		return db.insert(FlowProperty.of("Mass", units));
	}

	private Validation validate() {
		var v = Validation.on(db);
		v.run();
		return v;
	}

	private void assertWarning(ModelType type, long id) {
		var v = validate();
		var found = false;
		for (var item : v.items()) {
			if (item.type == Type.WARNING
					&& item.model != null
					&& item.model.type == type
					&& item.model.id == id
					&& item.message.contains("distribution")) {
				found = true;
				break;
			}
		}
		assertTrue("expected an uncertainty warning for " + type + " @" + id,
				found);
	}
}
