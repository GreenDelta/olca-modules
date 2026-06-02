package org.openlca.io.olca.systransfer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class SystemTransferUtilTest {

	@Test
	public void findMatchUsesInternalIdFirst() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var flow = Flow.product("product", mass);
		var process = Process.of("P", flow);

		var srcExchange = process.quantitativeReference.copy();
		var match = SystemTransferUtil.findMatch(srcExchange, process);

		assertEquals(process.quantitativeReference, match);
	}

	@Test
	public void findMatchFallsBackToFlowAndUnit() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var flow = Flow.product("product", mass);
		var process = Process.of("P", flow);

		var srcExchange = process.quantitativeReference.copy();
		srcExchange.internalId = 999;
		var match = SystemTransferUtil.findMatch(srcExchange, process);

		assertEquals(process.quantitativeReference, match);
	}
}
