package org.openlca.jsonld.input;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;

/**
 * When a unit group is updated/overwritten during import, the units need to be
 * synced, otherwise existing references are broken (e.g. in exchanges). This
 * test creates a simple database with a unit group, flow property, flow,
 * process, impact method and product system and imports the exact same unit
 * group with setting 'overwrite'. If the unit sync is successful, the database
 * validation should result in no errors.
 */
public class UnitSyncTest {

	private static final ModelType[] modelTypes = new ModelType[] {
		ModelType.IMPACT_METHOD,
		ModelType.PRODUCT_SYSTEM,
		ModelType.PROCESS
	};

	private final IDatabase db = Tests.getDb();
	private File allData;
	private File unitGroupData;

	@Before
	public void before() {
		db.clear();
		allData = SyncTestUtils.copyToTemp("unit_sync-all.zip");
		unitGroupData = SyncTestUtils.copyToTemp("unit_sync-unit_group.zip");
	}

	@After
	public void after() {
		SyncTestUtils.delete(unitGroupData);
		SyncTestUtils.delete(allData);
		db.clear();
	}

	@Test
	public void initialDataValidates() {
		SyncTestUtils.doImport(allData, db);
		Assert.assertTrue(validate());
	}

	@Test
	public void unitsSync() {
		SyncTestUtils.doImport(allData, db);
		SyncTestUtils.doImport(unitGroupData, db);
		Assert.assertTrue(validate());
	}

	private boolean validate() {
		return SyncTestUtils.validate(modelTypes, ref -> {
			if(!ref.type.equals(Unit.class.getCanonicalName()))
				return true;
			return db.get(Unit.class, ref.id) != null;
		});
	}

}
