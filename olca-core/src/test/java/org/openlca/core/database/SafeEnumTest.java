package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;

public class SafeEnumTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testFlowType() {
		var flow = db.insert(Flow.product("Some flow", null));
		var d = new FlowDao(db).getDescriptor(flow.id);
		assertEquals(FlowType.PRODUCT_FLOW, d.flowType);
		NativeSql.on(db).runUpdate("update tbl_flows set " +
				"flow_type = 'something else' where id = " + flow.id);
		d = new FlowDao(db).getDescriptor(flow.id);
		assertNull(d.flowType);
		// var flow = db.get(Flow.class, flowId); // this would fail
		NativeSql.on(db).runUpdate("update tbl_flows set " +
				"flow_type = null where id = " + flow.id);
		db.delete(flow);
	}

}
