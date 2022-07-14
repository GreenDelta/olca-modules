package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;

/**
 * When a process is updated/overwritten during import, the exchanges
 * need to be synced, otherwise existing references are broken (e.g. in
 * product systems). This test creates a simple database with a unit group, flow
 * property, flow, process and product system and imports the
 * exact same process with setting 'overwrite'. If the exchange sync is
 * successful, the database validation should result in no errors.
 */
public class ExchangeSyncTest {

	private IDatabase db;
	private File allData;
	private File processData;

	@Before
	public void before() throws IOException {
		db = Tests.getDb();
		db.clear();
		allData = SyncTestUtils.copyToTemp("exchange_sync-all.zip");
		processData = SyncTestUtils.copyToTemp("exchange_sync-process.zip");
	}

	@After
	public void after() throws IOException {
		SyncTestUtils.delete(processData);
		SyncTestUtils.delete(allData);
		db.clear();
	}

	@Test
	public void initialDataValidates() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.validate(db);
	}

	@Test
	public void exchangesSync() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(processData, db);
		SyncTestUtils.validate(db);
	}
}
