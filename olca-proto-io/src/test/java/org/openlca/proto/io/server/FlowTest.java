package org.openlca.proto.io.server;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowPropertyFactor;
import org.openlca.proto.ProtoFlowType;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.grpc.DataUpdateServiceGrpc;
import org.openlca.proto.grpc.ProtoDataSet;
import org.openlca.proto.io.Tests;

public class FlowTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testFlowIO() {

		var units = db.insert(UnitGroup.of("mass units", "kg"));
		var mass = db.insert(FlowProperty.of("mass", units));

		var massRef = ProtoRef.newBuilder()
			.setId(mass.refId)
			.build();
		var protoFlow = ProtoFlow.newBuilder()
			.setId(UUID.randomUUID().toString())
			.setFlowType(ProtoFlowType.PRODUCT_FLOW)
			.setName("my flow")
			.addFlowProperties(
				ProtoFlowPropertyFactor.newBuilder()
					.setFlowProperty(massRef)
					.setConversionFactor(1)
					.setIsRefFlowProperty(true))
			.build();

		// insert the flow
		ServiceTests.on(channel -> {
			var dataSet = ProtoDataSet.newBuilder()
				.setFlow(protoFlow)
				.build();
			var flowRef = DataUpdateServiceGrpc.newBlockingStub(channel)
				.put(dataSet);
			assertEquals(protoFlow.getId(), flowRef.getId());
		});

		var flow = db.get(Flow.class, protoFlow.getId());
		assertEquals(mass, flow.referenceFlowProperty);
		assertEquals(units.referenceUnit, flow.getReferenceUnit());

		db.delete(flow, mass, units);
	}

}
