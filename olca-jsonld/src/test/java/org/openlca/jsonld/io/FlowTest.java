package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class FlowTest extends AbstractZipTest {

	@Test
	public void testFlow() throws Exception {
		FlowDao dao = new FlowDao(Tests.getDb());
		Flow flow = createModel(dao);
		doExport(flow, dao);
		doImport(dao, flow);
		dao.delete(flow);
	}

	private Flow createModel(FlowDao dao) {
		Flow flow = new Flow();
		flow.setName("flow");
		flow.setRefId(UUID.randomUUID().toString());
		dao.insert(flow);
		return flow;
	}

	private void doExport(Flow flow, FlowDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(flow);
		});
		dao.delete(flow);
		Assert.assertFalse(dao.contains(flow.getRefId()));
	}

	private void doImport(FlowDao dao, Flow flow) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(flow.getRefId()));
		Flow clone = dao.getForRefId(flow.getRefId());
		Assert.assertEquals(flow.getName(), clone.getName());
	}
}
