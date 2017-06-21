package org.openlca.core.math;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResultProvider;

public class SelfLoopAnalysisTest {

	private FullResultProvider result;

	@Before
	public void setUp() {
		Process p1 = TestProcess
				.refProduct("p1", 1, "kg")
				.prodIn("p2", 1, "kg")
				.elemIn("f1", 1, "kg")
				.elemOut("f2", 2, "kg")
				.get();
		Process p2 = TestProcess
				.refProduct("p2", 1, "kg")
				.prodIn("p1", 0.5, "kg")
				.elemIn("f1", 2, "kg")
				.elemOut("f2", 1, "kg")
				.get();
		ProductSystem system = TestSystem.of(p1).link(p2).get();
		system.setTargetAmount(2);
		system = Tests.update(system);
		result = TestSystem.calculate(system);
	}

	@Test
	public void testTotalRequirements() {
		double[] tr = result.result.totalRequirements;
		Assert.assertEquals(4, tr[0], 1e-10);
		Assert.assertEquals(4, tr[1], 1e-10);
	}

	@Test
	public void testScalingVector() {
		double[] s = result.result.scalingFactors;
		Assert.assertEquals(4, s[0], 1e-10);
		Assert.assertEquals(4, s[1], 1e-10);
	}

	@Test
	public void testUpstreamResults() {
		Assert.assertEquals(12, upstream("p1", "f1"), 1e-10);
		Assert.assertEquals(12, upstream("p1", "f2"), 1e-10);
		Assert.assertEquals(10, upstream("p2", "f1"), 1e-10);
		Assert.assertEquals(8, upstream("p2", "f2"), 1e-10);
	}

	@Test
	public void testDirectResults() {
		Assert.assertEquals(4, direct("p1", "f1"), 1e-10);
		Assert.assertEquals(8, direct("p1", "f2"), 1e-10);
		Assert.assertEquals(8, direct("p2", "f1"), 1e-10);
		Assert.assertEquals(4, direct("p2", "f2"), 1e-10);
	}

	private double direct(String processName, String flowName) {
		return result.getSingleFlowResult(process(processName),
				flow(flowName)).value;
	}

	private double upstream(String processName, String flowName) {
		return result.getUpstreamFlowResult(
				process(processName), flow(flowName)).value;
	}

	private FlowDescriptor flow(String name) {
		for (FlowDescriptor f : result.getFlowDescriptors()) {
			if (name.equals(f.getName()))
				return f;
		}
		return null;
	}

	private ProcessDescriptor process(String name) {
		for (ProcessDescriptor p : result.getProcessDescriptors()) {
			if (name.equals(p.getName()))
				return p;
		}
		return null;
	}

}
