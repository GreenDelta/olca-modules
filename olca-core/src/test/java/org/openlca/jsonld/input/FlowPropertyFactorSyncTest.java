package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;

/**
 * When a flow is updated/overwritten during import, the flow property factors
 * need to be synced, otherwise existing references are broken (e.g. in
 * exchanges). This test creates a simple database with a unit group, flow
 * property, flow, process, impact method and product system and imports the
 * exact same flow with setting 'overwrite'. If the flow property factor sync is
 * successful, the database validation should result in no errors.
 */
public class FlowPropertyFactorSyncTest {

	private final IDatabase db = Tests.getDb();
	private File allData;
	private File flowData;

	@Before
	public void before() throws IOException {
		db.clear();
		allData = SyncTestUtils.copyToTemp("flow_property_factor_sync-all.zip");
		flowData = SyncTestUtils.copyToTemp("flow_property_factor_sync-flow.zip");
	}

	@After
	public void after() throws IOException {
		SyncTestUtils.delete(flowData);
		SyncTestUtils.delete(allData);
		db.clear();
	}

	@Test
	public void initialDataValidates() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.validate(db);
	}

	@Test
	public void flowPropertyFactorsSync() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(flowData, db);
		SyncTestUtils.validate(db);
	}
}
