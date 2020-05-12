package org.openlca.util;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
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
		dep1 = reload(process1).parameters.get(0);
		Assert.assertEquals("2 * global_param", dep1.formula);

		// should be still the same in process 2
		dep2 = reload(process2).parameters.stream()
				.filter(p -> !p.isInputParameter)
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(dep2);
		Assert.assertEquals("2 * param", dep2.formula);

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
		e1 = reload(p1).exchanges.get(0);
		Assert.assertEquals("2 * global_param", e1.formula);

		// should **not** be renamed in process 2
		e2 = reload(p2).exchanges.get(0);
		Assert.assertEquals("2 * param", e2.formula);
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
