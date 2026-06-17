package org.openlca.util;

import static org.junit.Assert.*;
import static org.openlca.util.Parameters.isValidName;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class ParametersTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testIsValidName() {

		// valid: single letter/underscore/dollar
		assertTrue(isValidName("a"));
		assertTrue(isValidName("Z"));
		assertTrue(isValidName("_"));
		assertTrue(isValidName("$"));

		// valid: multiple letters
		assertTrue(isValidName("abc"));
		assertTrue(isValidName("ABC"));
		assertTrue(isValidName("fooBar"));

		// valid: letters with digits
		assertTrue(isValidName("a1"));
		assertTrue(isValidName("var42"));
		assertTrue(isValidName("x2y3"));

		// valid: underscores and dollars in various positions
		assertTrue(isValidName("_foo"));
		assertTrue(isValidName("$bar"));
		assertTrue(isValidName("a_b"));
		assertTrue(isValidName("a$b"));
		assertTrue(isValidName("a_b$c"));
		assertTrue(isValidName("__"));
		assertTrue(isValidName("$$"));
		assertTrue(isValidName("_$"));
		assertTrue(isValidName("$_"));

		// invalid: null or empty
		assertFalse(isValidName(null));
		assertFalse(isValidName(""));
		assertFalse(isValidName("   "));

		// invalid: starts with digit
		assertFalse(isValidName("1"));
		assertFalse(isValidName("1abc"));
		assertFalse(isValidName("42foo"));

		// invalid: contains special characters
		assertFalse(isValidName("_+"));
		assertFalse(isValidName("a-b"));
		assertFalse(isValidName("a.b"));
		assertFalse(isValidName("a b"));
		assertFalse(isValidName("a!"));
		assertFalse(isValidName("foo@bar"));
		assertFalse(isValidName("a#b"));
		assertFalse(isValidName("a%"));
		assertFalse(isValidName("a^b"));
		assertFalse(isValidName("a(b)"));

		// invalid: leading whitespace is trimmed but inner space is invalid
		assertFalse(isValidName("a b"));
	}

	@Test
	public void testFindRedefOwners() {

		// create a process with a local parameter
		var process = TestProcess
				.refProduct("prod", 1, "kg")
				.param("param", 42)
				.get();
		var param = process.parameters.getFirst();

		// create project and product system
		var project = new Project();
		project.variants.add(new ProjectVariant());
		db.insert(project);
		var system = new ProductSystem();
		system.parameterSets.add(new ParameterRedefSet());
		db.insert(system);

		// should not find something
		var owners = Parameters.findRedefOwners(
				param, process, db);
		assertTrue(owners.isEmpty());

		// add parameter redefs
		var redef = new ParameterRedef();
		redef.name = param.name;
		redef.contextId = process.id;
		redef.contextType = ModelType.PROCESS;
		redef.value = 24;
		project.variants.getFirst()
				.parameterRedefs.add(redef.copy());
		project = db.update(project);
		system.parameterSets.getFirst()
				.parameters.add(redef.copy());
		system = db.update(system);

		// should find something
		owners = Parameters.findRedefOwners(
				param, process, db);
		assertEquals(2, owners.size());
		assertTrue(owners.contains(Descriptor.of(system)));
		assertTrue(owners.contains(Descriptor.of(project)));
		db.clear();
	}

	@Test
	public void testRenameInProcess() {
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var product = db.insert(Flow.product("p", mass));
		var process = Process.of("pp", product);
		process.parameters.add(Parameter.process("inp", 21));
		var dep = Parameter.process("dep", "inp * 2");
		process.parameters.add(dep);
		process.quantitativeReference.formula = "dep * 2";
		// make sure that dep is detached
		process = db.update(db.insert(process));
		db.clearCache();

		Parameters.rename(dep, process, db, "depp");

		process = db.get(Process.class, process.id);
		var depp = process.parameters.stream()
			.filter(p -> !p.isInputParameter)
			.findAny()
			.orElse(null);
		assertNotNull(depp);
		assertEquals("depp", depp.name);
		assertEquals("depp * 2", process.quantitativeReference.formula);

		db.delete(process, process, mass, units);
	}
}
