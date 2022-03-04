package org.openlca.io.simapro.csv.input;

import java.io.File;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.io.Tests;

public class ProcessImportTest {

	private final IDatabase db = Tests.getDb();
	private final File file = TestCsv.temporaryFileOf("process.csv");

	@Before
	public void before() {
		db.clear();
	}

	@After
	public void after() {
		assertTrue(file.delete());
	}

	@Test
	public void testProcessImport() {
		assertTrue(db.getAll(Process.class).isEmpty());
		new SimaProCsvImport(db, file).run();
		var process = db.getAll(Process.class).get(0);
		assertEquals("Test", process.name);
		assertEquals(6, process.exchanges.size());
	}

}
