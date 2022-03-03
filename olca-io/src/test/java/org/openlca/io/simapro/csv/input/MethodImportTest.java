package org.openlca.io.simapro.csv.input;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.io.Tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodImportTest {

	private final IDatabase db = Tests.getDb();
	private final File file = TestCsv.temporaryFileOf("method.csv");

	@Before
	public void before() {
		db.clear();
	}

	@After
	public void after() {
		assertTrue(file.delete());
	}

	@Test
	public void testMethodImport() {
		assertTrue(db.getAll(ImpactMethod.class).isEmpty());
		new SimaProCsvImport(db, file).run();
		var method = db.getAll(ImpactMethod.class).get(0);
		assertEquals("EN 15804:2012", method.name);
		assertEquals(2, method.impactCategories.size());
	}
}
