package org.openlca.util;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.RootEntity;

public class ParameterRenameTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRenameUnused() {
		var param = global("global_unused");
		var renamed = Parameters.rename(db, param, "unused_global");
		Assert.assertEquals(param, renamed);
		Assert.assertEquals("unused_global", renamed.name);
		drop(param);
	}

	@Test
	public void testParameterFormulas() {
		var global = global("param");
		var globalDep = global("dep");
		globalDep.isInputParameter = false;
		globalDep.formula = "2 / param";
		put(globalDep);

		var process1 = new Process();
		var dep1 = local(process1, "dep");
		dep1.isInputParameter = false;
		dep1.formula = "2 * param";
		put(process1);

		var process2 = new Process();
		local(process2, "param");
		var dep2 = local(process2, "dep");
		dep2.isInputParameter = false;
		dep2.formula = "2 * param";
		put(process2);

		global = Parameters.rename(db, global, "global_param");
		Assert.assertEquals("global_param", global.name);

		// should be renamed in global parameter formula
		globalDep = reload(globalDep);
		Assert.assertEquals("2 / global_param", globalDep.formula);

		// should be renamed in process 1
		process1 = reload(process1);
		dep1 = process1.parameters.get(0);
		Assert.assertEquals("2 * global_param", dep1.formula);
		Assert.assertTrue(process1.version > 0L);
		Assert.assertTrue(process1.lastChange > 0L);

		// should be still the same in process 2
		process2 = reload(process2);
		dep2 = process2.parameters.stream()
				.filter(p -> !p.isInputParameter)
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(dep2);
		Assert.assertEquals("2 * param", dep2.formula);
		Assert.assertEquals(process2.version, 0L);
		Assert.assertEquals(process2.lastChange, 0L);

		drop(process1);
		drop(process2);
		drop(global);
	}

	@Test
	public void testExchangeFormulas() {
		var global = global("param");

		// no local parameter in process 1
		var p1 = new Process();
		var e1 = new Exchange();
		e1.formula = "2 * param";
		p1.exchanges.add(e1);
		put(p1);

		// local parameter in process 2
		var p2 = new Process();
		local(p2, "param");
		var e2 = new Exchange();
		e2.formula = "2 * param";
		p2.exchanges.add(e2);
		put(p2);

		global = Parameters.rename(db, global, "global_param");
		Assert.assertEquals("global_param", global.name);

		// should be renamed in process 1
		p1 = reload(p1);
		e1 = p1.exchanges.get(0);
		Assert.assertEquals("2 * global_param", e1.formula);
		Assert.assertTrue(p1.version > 0L);
		Assert.assertTrue(p1.lastChange > 0L);

		// should **not** be renamed in process 2
		p2 = reload(p2);
		e2 = p2.exchanges.get(0);
		Assert.assertEquals("2 * param", e2.formula);
		Assert.assertEquals(p2.version, 0L);
		Assert.assertEquals(p2.lastChange, 0L);

		drop(p1);
		drop(p2);
		drop(global);
	}

	@Test
	public void testImpactFormulas() {
		var global = global("param");

		// no local parameter in impact 1
		var i1 = new ImpactCategory();
		var f1 = new ImpactFactor();
		f1.formula = "2 * param";
		i1.impactFactors.add(f1);
		put(i1);

		// local parameter in impact 2
		var i2 = new ImpactCategory();
		local(i2, "param");
		var f2 = new ImpactFactor();
		f2.formula = "2 * param";
		i2.impactFactors.add(f2);
		put(i2);

		global = Parameters.rename(db, global, "global_param");
		Assert.assertEquals("global_param", global.name);

		// should be renamed in impact 1
		i1 = reload(i1);
		f1 = i1.impactFactors.get(0);
		Assert.assertEquals("2 * global_param", f1.formula);
		Assert.assertTrue(i1.version > 0L);
		Assert.assertTrue(i1.lastChange > 0L);

		// should **not** be renamed in impact 2
		i2 = reload(i2);
		f2 = i2.impactFactors.get(0);
		Assert.assertEquals("2 * param", f2.formula);
		Assert.assertEquals(0L, i2.version);
		Assert.assertEquals(0L, i2.lastChange);

		drop(i1);
		drop(i2);
		drop(global);
	}

	@Test
	public void testProductSystemRedefs() {
		var global = global("param");

		var process = new Process();
		local(process, "param");
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
		Assert.assertEquals("global_param", global.name);

		system = reload(system);

		globalRedef = system.parameterSets.get(0)
				.parameters.stream()
				.filter(r -> r.contextId == null)
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(globalRedef);
		Assert.assertEquals("global_param", globalRedef.name);

		localRedef = system.parameterSets.get(0)
				.parameters.stream()
				.filter(r -> Objects.equals(r.contextId, process.id))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(localRedef);
		Assert.assertEquals("param", localRedef.name);

		Assert.assertTrue(system.version > 0);
		Assert.assertTrue(system.lastChange > 0);

		drop(system);
		drop(process);
		drop(global);
	}

	@Test
	public void testProjectVariantRedefs() {
		var global = global("param");

		var process = new Process();
		local(process, "param");
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
		Assert.assertEquals("global_param", global.name);

		project = reload(project);

		globalRedef = project.variants.get(0)
				.parameterRedefs.stream()
				.filter(r -> r.contextId == null)
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(globalRedef);
		Assert.assertEquals("global_param", globalRedef.name);

		localRedef = project.variants.get(0)
				.parameterRedefs.stream()
				.filter(r -> Objects.equals(r.contextId, process.id))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(localRedef);
		Assert.assertEquals("param", localRedef.name);

		Assert.assertTrue(project.version > 0);
		Assert.assertTrue(project.lastChange > 0);

		drop(project);
		drop(process);
		drop(global);
	}

	private Parameter global(String name) {
		var param = new Parameter();
		param.isInputParameter = true;
		param.name = name;
		param.scope = ParameterScope.GLOBAL;
		return new ParameterDao(db).insert(param);
	}

	private Parameter local(Process process, String name) {
		var param = new Parameter();
		param.isInputParameter = true;
		param.name = name;
		param.scope = ParameterScope.PROCESS;
		process.parameters.add(param);
		return param;
	}

	private Parameter local(ImpactCategory impact, String name) {
		var param = new Parameter();
		param.isInputParameter = true;
		param.name = name;
		param.scope = ParameterScope.IMPACT_CATEGORY;
		impact.parameters.add(param);
		return param;
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> void put(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		if (e.id == 0) {
			dao.insert(e);
		} else {
			dao.update(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> T reload(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		return dao.getForId(e.id);
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> void drop(T e) {
		var dao = (BaseDao<T>) Daos.base(db, e.getClass());
		dao.delete(e);
	}
}
