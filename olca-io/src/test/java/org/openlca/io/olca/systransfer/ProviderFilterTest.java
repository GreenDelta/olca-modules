package org.openlca.io.olca.systransfer;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

public class ProviderFilterTest {

	@Test
	public void includesReferenceProcess() {
		var process = new Process();
		process.id = 1413035;

		var exchange = new Exchange();
		exchange.id = 1;
		process.quantitativeReference = exchange;

		var system = ProductSystem.of("test system", process);
		var filter = ProviderFilter.of(system);

		assertTrue(filter.hasProcesses());
		assertTrue(filter.containsProcess(process.id));
		assertFalse(filter.hasResults());
		assertFalse(filter.hasSystems());
	}
}
