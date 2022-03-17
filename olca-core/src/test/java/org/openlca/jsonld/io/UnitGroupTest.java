package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class UnitGroupTest extends AbstractZipTest {

	@Test
	public void testUnitGroup() {
		UnitGroupDao dao = new UnitGroupDao(Tests.getDb());
		UnitGroup group = createModel(dao);
		doExport(group, dao);
		doImport(dao, group);
		dao.delete(group);
	}

	private UnitGroup createModel(UnitGroupDao dao) {
		UnitGroup group = new UnitGroup();
		group.name = "group";
		group.refId = UUID.randomUUID().toString();
		dao.insert(group);
		return group;
	}

	private void doExport(UnitGroup group, UnitGroupDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(group);
		});
		dao.delete(group);
		Assert.assertFalse(dao.contains(group.refId));
	}

	private void doImport(UnitGroupDao dao, UnitGroup group) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(group.refId));
		UnitGroup clone = dao.getForRefId(group.refId);
		Assert.assertEquals(group.name, clone.name);
	}
}
