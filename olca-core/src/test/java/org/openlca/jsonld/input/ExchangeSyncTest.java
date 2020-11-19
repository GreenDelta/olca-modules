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
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Tests;

/**
 * When a process is updated/overwritten during import, the exchanges
 * need to be synced, otherwise existing references are broken (e.g. in
 * product systems). This test creates a simple database with a unit group, flow
 * property, flow, process and product system and imports the
 * exact same process with setting 'overwrite'. If the exchange sync is
 * successful, the database validation should result in no errors.
 */
public class ExchangeSyncTest {

	private static final ModelType[] modelTypes = new ModelType[] {
		ModelType.PRODUCT_SYSTEM
	};
	private IDatabase db;
	private BaseDao<Exchange> dao;
	private File allData;
	private File processData;

	@Before
	public void before() throws IOException {
		db = Tests.getDb();
		Tests.clearDb();
		dao = Daos.base(db, Exchange.class);
		allData = SyncTestUtils.copyToTemp("exchange_sync-all.zip");
		processData = SyncTestUtils.copyToTemp("exchange_sync-process.zip");
	}

	@Test
	public void initialDataValidates() throws IOException {
		SyncTestUtils.doImport(allData, db);
		Assert.assertTrue(validate());
	}

	@Test
	public void exchangesSync() throws IOException {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(processData, db);
		Assert.assertTrue(validate());
	}

	private boolean validate() {
		return SyncTestUtils.validate(modelTypes, (reference) -> {
			if(reference.type.equals(Exchange.class.getCanonicalName()))
				return dao.getForId(reference.id) != null;
			return true;
		});
	}
	@After
	public void after() throws IOException {
		SyncTestUtils.delete(processData);
		SyncTestUtils.delete(allData);
		Tests.clearDb();
	}

}
