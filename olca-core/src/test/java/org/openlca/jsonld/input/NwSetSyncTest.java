package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;

/**
 * When an impact method is updated/overwritten during import, the nw sets need
 * to be synced, otherwise existing references are broken (e.g. in projects).
 * This test creates a simple database with an impact method and project and
 * imports the exact same impact method with setting 'overwrite'. If the nw set
 * sync is successful, the database validation should result in no errors.
 */
public class NwSetSyncTest {

	private final IDatabase db = Tests.getDb();
	private File allData;
	private File nwSetData;

	@Before
	public void before() throws IOException {
		db.clear();
		allData = SyncTestUtils.copyToTemp("nw_set_sync-all.zip");
		nwSetData = SyncTestUtils.copyToTemp("nw_set_sync-impact_method.zip");
	}

	@After
	public void after() throws IOException {
		SyncTestUtils.delete(nwSetData);
		SyncTestUtils.delete(allData);
		db.clear();
	}

	@Test
	public void initialDataValidates() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.validate(db);
	}

	@Test
	public void nwSetSync() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(nwSetData, db);
		SyncTestUtils.validate(db);
	}


}
