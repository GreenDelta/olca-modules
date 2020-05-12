package org.openlca.util;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

public class ParameterRenameTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRenameUnused() {
		var param = new Parameter();
		param.name = "global_unused";
		param.scope = ParameterScope.GLOBAL;
		new ParameterDao(db).insert(param);
		var renamed = Parameters.rename(db, param, "unused_global");
		Assert.assertEquals(param, renamed);
		Assert.assertEquals("unused_global", renamed.name);
	}
}
