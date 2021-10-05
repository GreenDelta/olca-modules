package org.openlca.core.model;

import org.junit.Assert;
import org.junit.Test;

public class AllocationFactorCloneTest {

	@Test
	public void testClone() {
		Process process = createProcess();
		Process clone = process.copy();
		checkGeneralValues(process, clone);
		checkAllocationFactor(clone);
	}

	private void checkGeneralValues(Process process, Process clone) {
		Assert.assertEquals(1, clone.allocationFactors.size());
		Assert.assertEquals(1, clone.exchanges.size());
		Assert.assertEquals(clone.quantitativeReference,
				clone.exchanges.get(0));
		Assert.assertNotEquals(clone.quantitativeReference,
				process.quantitativeReference);
		Assert.assertEquals(clone.quantitativeReference.flow,
				process.quantitativeReference.flow);
	}

	private void checkAllocationFactor(Process clone) {
		AllocationFactor fac = clone.allocationFactors.get(0);
		Assert.assertEquals(21d, fac.value, 1e-24);
		Assert.assertEquals(AllocationMethod.ECONOMIC, fac.method);
		Assert.assertEquals(3L, fac.productId);
		Assert.assertEquals(fac.exchange, clone.quantitativeReference);
		Assert.assertEquals(fac.exchange, clone.exchanges.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		Flow flow = new Flow();
		Exchange exchange = new Exchange();
		exchange.flow = flow;
		exchange.amount = 42d;
		process.exchanges.add(exchange);
		process.quantitativeReference = exchange;
		AllocationFactor factor = new AllocationFactor();
		factor.productId = (long) 3;
		factor.exchange = exchange;
		factor.method = AllocationMethod.ECONOMIC;
		factor.value = 21d;
		process.allocationFactors.add(factor);
		return process;
	}
}
