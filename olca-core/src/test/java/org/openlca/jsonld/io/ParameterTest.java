package org.openlca.jsonld.io;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ParameterTest extends AbstractZipTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testGlobal() {
		var param = Parameter.global("param", "1+1");
		db.insert(param);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(param);
		});
		db.delete(param);
		with(zip -> new JsonImport(zip, db).run());
		var copy = db.get(Parameter.class, param.refId);
		assertEquals(param.refId, copy.refId);
		db.delete(copy);
	}

	@Test
	public void testProcess() {
		var process = new Process();
		process.refId = UUID.randomUUID().toString();
		var param = process.parameter("param", 42);
		db.insert(process);
		with(zip -> new JsonExport(db, zip).write(process));
		db.delete(process);
		with(zip -> new JsonImport(zip, db).run());
		var clone = db.get(Process.class, process.refId);
		assertEquals(param.refId, clone.parameters.get(0).refId);
		db.delete(clone);
	}

	@Test
	public void testImpactCategory() {
		var impact = new ImpactCategory();
		impact.refId = UUID.randomUUID().toString();
		var param = impact.parameter("param", 42);
		db.insert(impact);
		with(zip ->  new JsonExport(Tests.getDb(), zip).write(impact));
		db.delete(impact);
		with(zip -> new JsonImport(zip, Tests.getDb()).run());
		var clone = db.get(ImpactCategory.class, impact.refId);
		assertEquals(param.refId, clone.parameters.get(0).refId);
		db.delete(clone);
	}
}
