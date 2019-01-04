package org.openlca.core.model;

import org.junit.Assert;
import org.junit.Test;

public class AllocationFactorCloneTest {

	@Test
	public void testClone() {
		Process process = createProcess();
		Process clone = process.clone();
		checkGeneralValues(process, clone);
		checkAllocationFactor(clone);
	}

	private void checkGeneralValues(Process process, Process clone) {
		Assert.assertEquals(1, clone.getAllocationFactors().size());
		Assert.assertEquals(1, clone.getExchanges().size());
		Assert.assertEquals(clone.getQuantitativeReference(),
				clone.getExchanges().get(0));
		Assert.assertNotEquals(clone.getQuantitativeReference(),
				process.getQuantitativeReference());
		Assert.assertEquals(clone.getQuantitativeReference().flow,
				process.getQuantitativeReference().flow);
	}

	private void checkAllocationFactor(Process clone) {
		AllocationFactor fac = clone.getAllocationFactors().get(0);
		Assert.assertEquals(21d, fac.value, 1e-24);
		Assert.assertEquals(AllocationMethod.ECONOMIC, fac.method);
		Assert.assertEquals(3L, fac.productId);
		Assert.assertEquals(fac.exchange, clone.getQuantitativeReference());
		Assert.assertEquals(fac.exchange, clone.getExchanges().get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		Flow flow = new Flow();
		Exchange exchange = new Exchange();
		exchange.flow = flow;
		exchange.amount = 42d;
		process.getExchanges().add(exchange);
		process.setQuantitativeReference(exchange);
		AllocationFactor factor = new AllocationFactor();
		factor.productId = (long) 3;
		factor.exchange = exchange;
		factor.method = AllocationMethod.ECONOMIC;
		factor.value = 21d;
		process.getAllocationFactors().add(factor);
		return process;
	}
}
