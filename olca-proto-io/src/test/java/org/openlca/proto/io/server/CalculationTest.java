package org.openlca.proto.io.server;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoFlowType;
import org.openlca.proto.grpc.ProtoCalculationSetup;
import org.openlca.proto.grpc.ResultServiceGrpc;
import org.openlca.proto.grpc.ResultValue;
import org.openlca.proto.io.Tests;
import org.openlca.proto.io.output.Refs;

public class CalculationTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testSimpleProcess() {

		var units = db.insert(UnitGroup.of("units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("mass", units));
		var e = db.insert(Flow.elementary("e", mass));
		var p = db.insert(Flow.product("p", mass));
		var process = Process.of("P", p);
		process.output(e, 21);
		db.insert(process);
		var system = db.insert(ProductSystem.of(process));

		var impact = ImpactCategory.of("gwp", "CO2eq");
		impact.factor(e, 2);
		db.insert(impact);
		var method = ImpactMethod.of("method");
		method.impactCategories.add(impact);
		db.insert(method);

		Consumer<ResultValue> lciCheck = value -> {
			var flowRef = value.getEnviFlow().getFlow();
			assertEquals("e", flowRef.getName());
			assertEquals(ProtoFlowType.ELEMENTARY_FLOW, flowRef.getFlowType());
			assertEquals("kg", flowRef.getRefUnit());
			assertEquals(42, value.getValue(), 1e-16);
		};

		Consumer<ResultValue> lciaCheck = value -> {
			assertEquals("gwp", value.getImpact().getName());
			assertEquals("CO2eq", value.getImpact().getRefUnit());
			assertEquals(84, value.getValue(), 1e-16);
		};

		// direct process calculation
		ServiceTests.on(channel -> {
			var stub = ResultServiceGrpc.newBlockingStub(channel);
			var setup = ProtoCalculationSetup.newBuilder()
				.setProcess(Refs.refOf(process))
				.setImpactMethod(Refs.refOf(method))
				.setAmount(2)
				.build();
			var result = stub.calculate(setup);
			lciCheck.accept(stub.getTotalInventory(result).next());
			lciaCheck.accept(stub.getTotalImpacts(result).next());
		});

		// product system calculation
		ServiceTests.on(channel -> {
			var stub = ResultServiceGrpc.newBlockingStub(channel);
			var setup = ProtoCalculationSetup.newBuilder()
				.setProductSystem(Refs.refOf(system))
				.setImpactMethod(Refs.refOf(method))
				.setAmount(2)
				.build();
			var result = stub.calculate(setup);
			lciCheck.accept(stub.getTotalInventory(result).next());
			lciaCheck.accept(stub.getTotalImpacts(result).next());
		});

		db.delete(method, impact, system, process, p, e, mass, units);
	}

}
