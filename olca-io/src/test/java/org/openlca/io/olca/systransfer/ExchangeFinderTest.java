package org.openlca.io.olca.systransfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.olca.TransferContext;

public class ExchangeFinderTest {

	private final IDatabase source = Derby.createInMemory();
	private final IDatabase target = Derby.createInMemory();

	@After
	public void cleanup() throws Exception {
		source.close();
		target.close();
	}

	@Test
	public void findExchangeId() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		source.insert(units, mass);

		var product = Flow.product("product", mass);
		var inputFlow = Flow.product("input", mass);
		var process = Process.of("P", product);
		var input = process.input(inputFlow, 2.0);
		source.insert(product, inputFlow, process);

		var ctx = TransferContext.create(source, target);
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
