package org.openlca.util;

import java.util.UUID;

import javax.xml.transform.Source;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Unit;

public class RefIdMapTest {

	private IDatabase db = Tests.getDb();
	private Flow flow;

	@Before
	public void setUp() {
		flow = new Flow();
		flow.refId = UUID.randomUUID().toString();
		flow = new FlowDao(db).insert(flow);
	}

	@After
	public void tearDown() {
		new FlowDao(db).delete(flow);
	}

	@Test
	public void testFindRefId() {
		RefIdMap<Long, String> idMap = RefIdMap.internalToRef(db, Flow.class,
				Unit.class);
		String flowRefId = idMap.get(Flow.class, flow.id);
		Assert.assertEquals(flowRefId, flow.refId);
		Assert.assertNull(idMap.get(Unit.class, -42L));
		Assert.assertNull(idMap.get(Source.class, -42L));
	}

	@Test
	public void testFindInternalId() {
		RefIdMap<String, Long> idMap = RefIdMap.refToInternal(db, Flow.class,
				Unit.class);
		Long flowId = idMap.get(Flow.class, flow.refId);
		Assert.assertEquals(flowId.longValue(), flow.id);
		Assert.assertNull(idMap.get(Unit.class, "abc"));
		Assert.assertNull(idMap.get(Source.class, "abc"));
	}

}
