package org.openlca.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Objects;

import org.junit.Test;
import org.openlca.core.TestData;
import org.openlca.core.TestProcess;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.RefEntity;

public class ParameterRenameTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRenameUnused() {
		var param = db.insert(Parameter.global("global_unused", 42));
		var renamed = Parameters.rename(db, param, "unused_global");
		assertEquals(param, renamed);
		assertEquals("unused_global", renamed.name);
		drop(param);
	}

	@Test
	public void testParameterFormulas() {
		var global = db.insert(Parameter.global("param", 42));
		var globalDep = db.insert(Parameter.global("dep", "2 / param"));

		var process1 = new Process();
		process1.parameter("dep", "2 * param");
		put(process1);

		var process2 = new Process();
		process2.parameter("param", 42);
		process2.parameter("dep", "2 * param");
		put(process2);

		global = Parameters.rename(db, global, "global_param");
		assertEquals("global_param", global.name);

		// should be renamed in global parameter formula
		globalDep = reload(globalDep);
		assertEquals("2 / global_param", globalDep.formula);

		// should be renamed in process 1
		process1 = reload(process1);
		var dep1 = process1.parameters.getFirst();
		assertEquals("2 * global_param", dep1.formula);
		assertTrue(process1.version > 0L);
		assertTrue(process1.lastChange > 0L);

		// should be still the same in process 2
		process2 = reload(process2);
		var dep2 = process2.parameters.stream()
				.filter(p -> !p.isInputParameter)
				.findFirst()
				.orElse(null);
		assertNotNull(dep2);
		assertEquals("2 * param", dep2.formula);
		assertEquals(0L, process2.version);
		assertEquals(0L, process2.lastChange);

		drop(process1);
		drop(process2);
		drop(global);
	}

	@Test
	public void testExchangeFormulas() {
		var global = db.insert(Parameter.global("param", 42));

		// no local parameter in process 1
		var p1 = new Process();
		var e1 = new Exchange();
		e1.formula = "2 * param";
		p1.add(e1);
		put(p1);

		// local parameter in process 2
		var p2 = new Process();
		p2.parameter("param", 42);
		var e2 = new Exchange();
		e2.formula = "2 * param";
		p2.add(e2);
		put(p2);

		global = Parameters.rename(db, global, "global_param");
		assertEquals("global_param", global.name);

		// should be renamed in process 1
		p1 = reload(p1);
		e1 = p1.exchanges.getFirst();
		assertEquals("2 * global_param", e1.formula);
		assertTrue(p1.version > 0L);
		assertTrue(p1.lastChange > 0L);

		// should **not** be renamed in process 2
		p2 = reload(p2);
		e2 = p2.exchanges.getFirst();
		assertEquals("2 * param", e2.formula);
		assertEquals(0L, p2.version);
		assertEquals(0L, p2.lastChange);

		drop(p1);
		drop(p2);
		drop(global);
	}

	@Test
	public void testImpactFormulas() {
		var global = db.insert(Parameter.global("param", 42));

		// no local parameter in impact 1
		var i1 = new ImpactCategory();
		var f1 = new ImpactFactor();
		f1.formula = "2 * param";
		i1.impactFactors.add(f1);
		put(i1);

		// local parameter in impact 2
		var i2 = new ImpactCategory();
		i2.parameter("param", 42);
		var f2 = new ImpactFactor();
		f2.formula = "2 * param";
		i2.impactFactors.add(f2);
		put(i2);

		global = Parameters.rename(db, global, "global_param");
		assertEquals("global_param", global.name);

		// should be renamed in impact 1
		i1 = reload(i1);
		f1 = i1.impactFactors.getFirst();
		assertEquals("2 * global_param", f1.formula);
		assertTrue(i1.version > 0L);
		assertTrue(i1.lastChange > 0L);

		// should **not** be renamed in impact 2
		i2 = reload(i2);
		f2 = i2.impactFactors.getFirst();
		assertEquals("2 * param", f2.formula);
		assertEquals(0L, i2.version);
		assertEquals(0L, i2.lastChange);

		drop(i1);
		drop(i2);
		drop(global);
	}

	@Test
	public void testAllocationFactors() {
		var global = db.insert(Parameter.global("global_param", 42));
		var process = TestProcess
				.refProduct("p1", 1, "kg")
				.param("local_param", 33)
				.prodOut("p2", 1, "kg")
				.alloc("p1", AllocationMethod.PHYSICAL, "2 * global_param")
				.alloc("p2", AllocationMethod.ECONOMIC, "2 * local_param")
				.get();
		Parameters.rename(db, global, "glob_p");
		process = reload(process);

		var renamed = process.allocationFactors.stream()
				.filter(af -> af.method == AllocationMethod.PHYSICAL)
				.map(af -> af.formula)
				.findFirst()
				.orElse(null);
		assertEquals("2 * glob_p", renamed);

		var stillSame = process.allocationFactors.stream()
				.filter(af -> af.method == AllocationMethod.ECONOMIC)
				.map(af -> af.formula)
				.findFirst()
				.orElse(null);
		assertEquals("2 * local_param", stillSame);
	}

	@Test
	public void testProductSystemRedefs() {
		var global = db.insert(Parameter.global("param", 42));

		var process = new Process();
		process.parameter("param", 42);
		put(process);

		var system = new ProductSystem();
		var paramSet = new ParameterRedefSet();
		system.parameterSets.add(paramSet);

		var globalRedef = new ParameterRedef();
		globalRedef.name = "param";
		paramSet.parameters.add(globalRedef);

		var localRedef = new ParameterRedef();
		localRedef.name = "param";
		localRedef.contextId = process.id;
		localRedef.contextType = ModelType.PROCESS;
		paramSet.parameters.add(localRedef);

		put(system);

		global = Parameters.rename(db, global, "global_param");
		assertEquals("global_param", global.name);

		system = reload(system);

		globalRedef = system.parameterSets.getFirst()
				.parameters.stream()
				.filter(r -> r.contextId == null)
				.findFirst()
				.orElse(null);
		assertNotNull(globalRedef);
		assertEquals("global_param", globalRedef.name);

		localRedef = system.parameterSets.getFirst()
				.parameters.stream()
				.filter(r -> Objects.equals(r.contextId, process.id))
				.findFirst()
				.orElse(null);
		assertNotNull(localRedef);
		assertEquals("param", localRedef.name);

		assertTrue(system.version > 0);
		assertTrue(system.lastChange > 0);

		drop(system);
		drop(process);
		drop(global);
	}

	@Test
	public void testProjectVariantRedefs() {
		var global = db.insert(Parameter.global("param", 42));

		var process = new Process();
		process.parameter("param", 42);
		put(process);

		var project = new Project();
		var variant = new ProjectVariant();
		project.variants.add(variant);

		var globalRedef = new ParameterRedef();
		globalRedef.name = "param";
		variant.parameterRedefs.add(globalRedef);

		var localRedef = new ParameterRedef();
		localRedef.name = "param";
		localRedef.contextId = process.id;
		localRedef.contextType = ModelType.PROCESS;
		variant.parameterRedefs.add(localRedef);

		put(project);

		global = Parameters.rename(db, global, "global_param");
		assertEquals("global_param", global.name);

		project = reload(project);

		globalRedef = project.variants.getFirst()
				.parameterRedefs.stream()
				.filter(r -> r.contextId == null)
				.findFirst()
				.orElse(null);
		assertNotNull(globalRedef);
		assertEquals("global_param", globalRedef.name);

		localRedef = project.variants.getFirst()
				.parameterRedefs.stream()
				.filter(r -> Objects.equals(r.contextId, process.id))
				.findFirst()
				.orElse(null);
		assertNotNull(localRedef);
		assertEquals("param", localRedef.name);

		assertTrue(project.version > 0);
		assertTrue(project.lastChange > 0);

		drop(project);
		drop(process);
		drop(global);
	}

	@Test
	public void testRenameInProcess() {
		var product = TestData.flow("product", "kg", FlowType.PRODUCT_FLOW);
		var process = Process.of("process", product);
		var param = process.parameter("param", 42);
		process.quantitativeReference.formula = "2 * param";
		process.parameter("dep", "2 * param");
		var af = new AllocationFactor();
		af.formula = "2 * param";
		process.allocationFactors.add(af);
		db.insert(process);

		process = Parameters.rename(param, process, db, "new_param");
		var dep = process.parameters.stream()
				.filter(p -> !p.isInputParameter)
				.findFirst();
		assertTrue(dep.isPresent());
		List.of(dep.get().formula,
				process.exchanges.getFirst().formula,
				process.allocationFactors.getFirst().formula)
				.forEach(formula -> assertEquals("2 * new_param", formula));
	}

	@SuppressWarnings("unchecked")
	private <T extends RefEntity> void put(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		if (e.id == 0) {
			dao.insert(e);
		} else {
			dao.update(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends RefEntity> T reload(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		return dao.getForId(e.id);
	}

	@SuppressWarnings("unchecked")
	private <T extends RefEntity> void drop(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		dao.delete(e);
	}
}
