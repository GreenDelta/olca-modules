package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.ParameterReader;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.jsonld.output.ParameterWriter;

/**
 * Make sure that a global redefined parameter is exported and imported again.
 */
public class ParameterRedefTest extends AbstractZipTest {

	private final IDatabase db = Tests.getDb();
	private Parameter globalParam;
	private ParameterDao paramDao;
	private ParameterRedef redef;

	@Before
	public void setup() {
		paramDao = new ParameterDao(db);

		globalParam = new Parameter();
		globalParam.refId = UUID.randomUUID().toString();
		globalParam.isInputParameter = true;
		globalParam.name = "R";
		globalParam.value = 8.314;
		globalParam = paramDao.insert(globalParam);

		redef = new ParameterRedef();
		redef.name = "R";
		redef.value = 42;
	}

	@After
	public void tearDown() {
		db.clear();
	}

	@Test
	public void testInProductSystem() {

		// create the model
		var sys = new ProductSystem();
		sys.refId = UUID.randomUUID().toString();
		sys.parameterSets.add(ParameterRedefSet.of("baseline", redef));
		db.insert(sys);

		// write and clear DB
		with(zip -> new JsonExport(db, zip).write(sys));
		db.clear();
		assertNull(db.get(ProductSystem.class, sys.refId));
		assertNull(paramDao.getForRefId(globalParam.refId));

		// import and check
		with(zip -> new JsonImport(zip, db).run());
		var copy = db.get(ProductSystem.class, sys.refId);
		assertEquals("R", copy.parameterSets.get(0).parameters.get(0).name);
		Parameter p = paramDao.getForRefId(globalParam.refId);
		assertEquals("R", p.name);
	}

	@Test
	public void testInParameterRedefSet() {

		// create the model
		ProductSystemDao dao = new ProductSystemDao(db);
		ProductSystem sys = new ProductSystem();
		sys.refId = UUID.randomUUID().toString();
		ParameterRedefSet paramSet = new ParameterRedefSet();
		sys.parameterSets.add(paramSet);
		paramSet.isBaseline = true;
		paramSet.name = "Baseline";
		paramSet.parameters.add(redef);
		dao.insert(sys);

		// write and clear DB
		with(zip -> new JsonExport(Tests.getDb(), zip).write(sys));
		db.clear();
		assertNull(dao.getForRefId(sys.refId));
		assertNull(paramDao.getForRefId(globalParam.refId));

		// import and check
		with(zip -> new JsonImport(zip, db).run());
		ProductSystem sys2 = dao.getForRefId(sys.refId);
		assertEquals("R", sys2.parameterSets.get(0).parameters.get(0).name);
		Parameter p = paramDao.getForRefId(globalParam.refId);
		assertEquals("R", p.name);
	}

	@Test
	public void testContexts() {
		var contexts = List.of(
				db.insert(ImpactCategory.of("test")),
				db.insert(Process.of("test", null)));
		for (var context : contexts) {
			var param = Parameter.impact("param", 42);
			var redef = ParameterRedef.of(param, context, 42);
			redef.uncertainty = Uncertainty.uniform(21, 84);
			var export = new JsonExport(db, new MemStore());
			var json = ParameterWriter.mapRedefs(export, List.of(redef));
			redef = ParameterReader.readRedefs(json, DbEntityResolver.of(db)).get(0);
			assertEquals("param", redef.name);
			assertEquals(ModelType.of(context), redef.contextType);
			assertEquals(context.id, redef.contextId.longValue());
			assertEquals(42, redef.value, 1e-10);
			assertEquals(Uncertainty.uniform(21, 84), redef.uncertainty);
			db.delete(context);
		}
	}

}
