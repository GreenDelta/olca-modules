package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class FlowPropertyTest extends AbstractZipTest {

	@Test
	public void testFlowProperty() {
		FlowPropertyDao dao = new FlowPropertyDao(Tests.getDb());
		FlowProperty property = createModel(dao);
		doExport(property, dao);
		doImport(dao, property);
		dao.delete(property);
	}

	private FlowProperty createModel(FlowPropertyDao dao) {
		FlowProperty property = new FlowProperty();
		property.name = "property";
		property.refId = UUID.randomUUID().toString();
		dao.insert(property);
		return property;
	}

	private void doExport(FlowProperty property, FlowPropertyDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(property);
		});
		dao.delete(property);
		Assert.assertFalse(dao.contains(property.refId));
	}

	private void doImport(FlowPropertyDao dao, FlowProperty property) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(property.refId));
		FlowProperty clone = dao.getForRefId(property.refId);
		Assert.assertEquals(property.name, clone.name);
	}
}
