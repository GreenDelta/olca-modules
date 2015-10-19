package org.openlca.jsonld;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ParameterTest extends AbstractZipTest {

	private ParameterDao dao = new ParameterDao(Tests.getDb());

	@Test
	public void testGlobal() throws Exception {
		Parameter p = createParam(ParameterScope.GLOBAL);
		dao.insert(p);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(p);
		});
		dao.delete(p);
		Assert.assertFalse(dao.contains(p.getRefId()));
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(p.getRefId()));
		dao.delete(p);
	}

	private Parameter createParam(ParameterScope scope) {
		Parameter param = new Parameter();
		param.setRefId(UUID.randomUUID().toString());
		param.setName("param");
		param.setScope(scope);
		param.setValue(42);
		param.setInputParameter(false);
		param.setFormula("21 + 21");
		return param;
	}

}
