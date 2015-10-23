package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class UnitGroupTest extends AbstractZipTest {

	@Test
	public void testUnitGroup() throws Exception {
		UnitGroupDao dao = new UnitGroupDao(Tests.getDb());
		UnitGroup group = createModel(dao);
		doExport(group, dao);
		doImport(dao, group);
		dao.delete(group);
	}

	private UnitGroup createModel(UnitGroupDao dao) {
		UnitGroup group = new UnitGroup();
		group.setName("group");
		group.setRefId(UUID.randomUUID().toString());
		dao.insert(group);
		return group;
	}

	private void doExport(UnitGroup group, UnitGroupDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(group);
		});
		dao.delete(group);
		Assert.assertFalse(dao.contains(group.getRefId()));
	}

	private void doImport(UnitGroupDao dao, UnitGroup group) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(group.getRefId()));
		UnitGroup clone = dao.getForRefId(group.getRefId());
		Assert.assertEquals(group.getName(), clone.getName());
	}
}
