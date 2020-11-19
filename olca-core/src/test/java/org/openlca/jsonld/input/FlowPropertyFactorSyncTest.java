package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Tests;

/**
 * When a flow is updated/overwritten during import, the flow property factors
 * need to be synced, otherwise existing references are broken (e.g. in
 * exchanges). This test creates a simple database with a unit group, flow
 * property, flow, process, impact method and product system and imports the
 * exact same flow with setting 'overwrite'. If the flow property factor sync is
 * successful, the database validation should result in no errors.
 */
public class FlowPropertyFactorSyncTest {

	private static final ModelType[] modelTypes = new ModelType[] {
		ModelType.IMPACT_METHOD, ModelType.PRODUCT_SYSTEM, ModelType.PROCESS
	};
	private IDatabase db;
	private BaseDao<FlowPropertyFactor> dao;
	private File allData;
	private File flowData;

	@Before
	public void before() throws IOException {
		db = Tests.getDb();
		Tests.clearDb();
		dao = Daos.base(db, FlowPropertyFactor.class);
		allData = SyncTestUtils.copyToTemp("flow_property_factor_sync-all.zip");
		flowData = SyncTestUtils.copyToTemp("flow_property_factor_sync-flow.zip");
	}

	@Test
	public void initialDataValidates() throws IOException {
		SyncTestUtils.doImport(allData, db);
		Assert.assertTrue(validate());
	}

	@Test
	public void flowPropertyFactorsSync() throws IOException {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(flowData, db);
		Assert.assertTrue(validate());
	}

	private boolean validate() {
		return SyncTestUtils.validate(modelTypes, (reference) -> {
			if(!reference.type.equals(FlowPropertyFactor.class.getCanonicalName()))
				return true;
			return dao.getForId(reference.id) != null;
		});
	}
	@After
	public void after() throws IOException {
		SyncTestUtils.delete(flowData);
		SyncTestUtils.delete(allData);
		Tests.clearDb();
	}

}
