package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.jsonld.Tests;

/**
 * When an impact method is updated/overwritten during import, the nw sets need
 * to be synced, otherwise existing references are broken (e.g. in projects).
 * This test creates a simple database with an impact method and project and
 * imports the exact same impact method with setting 'overwrite'. If the nw set
 * sync is successful, the database validation should result in no errors.
 */
public class NwSetSyncTest {

	private static final ModelType[] modelTypes = new ModelType[] { ModelType.PROJECT };
	private IDatabase db;
	private NwSetDao dao;
	private File allData;
	private File nwSetData;

	@Before
	public void before() throws IOException {
		db = Tests.getDb();
		Tests.clearDb();
		dao = new NwSetDao(db);
		allData = SyncTestUtils.copyToTemp("nw_set_sync-all.zip");
		nwSetData = SyncTestUtils.copyToTemp("nw_set_sync-impact_method.zip");
	}

	@Test
	public void initialDataValidates() throws IOException {
		SyncTestUtils.doImport(allData, db);
		Assert.assertTrue(validate());
	}

	@Test
	public void nwSetSync() throws IOException {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(nwSetData, db);
		Assert.assertTrue(validate());
	}

	private boolean validate() {
		return SyncTestUtils.validate(db, modelTypes, (reference) -> {
			if (reference.type.equals(NwSet.class.getCanonicalName()))
				return dao.getForId(reference.id) != null;
			return true;
		});
	}

	@After
	public void after() throws IOException {
		SyncTestUtils.delete(nwSetData);
		SyncTestUtils.delete(allData);
		Tests.clearDb();
	}

}
