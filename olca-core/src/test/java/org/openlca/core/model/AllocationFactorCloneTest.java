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
		Assert.assertEquals(21d, fac.getValue(), 1e-24);
		Assert.assertEquals(AllocationMethod.ECONOMIC, fac.getAllocationType());
		Assert.assertEquals(3L, fac.getProductId());
		Assert.assertEquals(fac.getExchange(), clone.getQuantitativeReference());
		Assert.assertEquals(fac.getExchange(), clone.getExchanges().get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		Flow flow = new Flow();
		Exchange exchange = new Exchange();
		final Flow flow1 = flow;
		exchange.flow = flow1;
		exchange.amount = 42d;
		process.getExchanges().add(exchange);
		process.setQuantitativeReference(exchange);
		AllocationFactor factor = new AllocationFactor();
		factor.setProductId(3);
		factor.setExchange(exchange);
		factor.setAllocationType(AllocationMethod.ECONOMIC);
		factor.setValue(21d);
		process.getAllocationFactors().add(factor);
		return process;
	}
}
