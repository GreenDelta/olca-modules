package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class FlowTest extends AbstractZipTest {

	FlowDao dao = new FlowDao(Tests.getDb());

	@Test
	public void testFlow() {
		Flow flow = dao.insert(createModel());
		exportAndDelete(flow, dao);
		doImport(dao, flow);
		dao.delete(flow);
	}

	@Test
	public void testWithLocation() {
		Location loc = new Location();
		loc.code = "ABC";
		loc.refId = UUID.randomUUID().toString();
		LocationDao locDao = new LocationDao(Tests.getDb());
		loc = locDao.insert(loc);
		Flow flow = createModel();
		flow.location = loc;
		flow = dao.insert(flow);
		exportAndDelete(flow, dao);
		doImport(dao, flow);
		Flow clone = dao.getForRefId(flow.refId);
		Assert.assertEquals("ABC", clone.location.code);
		dao.delete(clone);
		locDao.delete(loc);
	}

	private Flow createModel() {
		Flow flow = new Flow();
		flow.name = "flow";
		flow.refId = UUID.randomUUID().toString();
		return flow;
	}

	private void exportAndDelete(Flow flow, FlowDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(flow);
		});
		dao.delete(flow);
		Assert.assertFalse(dao.contains(flow.refId));
	}

	private void doImport(FlowDao dao, Flow flow) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(flow.refId));
		Flow clone = dao.getForRefId(flow.refId);
		Assert.assertEquals(flow.name, clone.name);
	}
}
