package org.openlca.io.olca.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.olca.TestTransferContext;
import org.openlca.io.olca.TransferContext;

public class ExchangeFinderTest {

	private final TestTransferContext c = TestTransferContext.get();

	@After
	public void cleanup() throws Exception {
		c.clear();
	}

	@Test
	public void findExchangeId() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		c.source().insert(units, mass);

		var product = Flow.product("product", mass);
		var inputFlow = Flow.product("input", mass);
		var process = Process.of("P", product);
		var input = process.input(inputFlow, 2.0);
		c.source().insert(product, inputFlow, process);

		var ctx = TransferContext.create(c.source(), c.target());
		var copy = ctx.resolve(process);
		assertNotNull(copy);

		var targetExchange = copy.getExchange(input.internalId);
		assertNotNull(targetExchange);

		var link = new ProcessLink();
		link.processId = process.id;
		link.exchangeId = input.id;

		long found = ExchangeFinder.of(ctx).find(link);
		assertEquals(targetExchange.id, found);
	}
}
