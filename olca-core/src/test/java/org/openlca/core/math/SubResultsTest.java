package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.linking.SubSystemLinker;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.TagResult;
import org.openlca.util.Pair;

public class SubResultsTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProcessTag() {
		var p = new Process();
		p.tags = "abc";
		db.insert(p);
		var d = db.getDescriptor(Process.class, p.id);
		assertEquals("abc", d.tags);
	}

	/**
	 * Calculate a simple sub-system model:
	 * <pre>
	 * {@code
	 * 		A [1] -> [2] B [0.5] -> [2] C [1]
	 *
	 * 		expected total CO2 result:
	 * 		C: 1 * 1 kg CO2
	 * 		B: 4 * 1 kg CO2
	 * 		A: 8 * 1 kg CO2
	 * 		----------------
	 * 		13 kg CO2
	 * }
	 * </pre>
	 */
	@Test
	public void testSubResults() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var co2 = Flow.elementary("CO2", mass);
		var prodA = Flow.product("A", mass);
		var prodB = Flow.product("B", mass);
		var prodC = Flow.product("C", mass);

		// A [1] -> [2] B [0.5] -> [2] C [1]
		var procA = Process.of("A", prodA);
		var procB = Process.of("B", prodB);
		procB.quantitativeReference.amount = 0.5;
		procB.input(prodA, 2);
		var procC = Process.of("C", prodC);
		procC.input(prodB, 2);
		List.of(procA, procB, procC).forEach(p -> {
			p.output(co2, 1);
			p.tags = p.name;
		});

		db.insert(
			units, mass, co2,
			prodA, prodB, prodC,
			procA, procB, procC);

		var systems = Stream.of(procA, procB, procC)
			.map(p -> {
				var linker = new SubSystemLinker(db);
				var system = new ProductSystemBuilder(linker)
					.build(p);
				return db.insert(system);
			}).toList();
		var sysA = systems.get(0);
		var sysB = systems.get(1);
		var sysC = systems.get(2);

		var setup = CalculationSetup.fullAnalysis(sysC);
		var resultC = new SystemCalculator(db).calculateFull(setup);
		var co2Idx = resultC.enviIndex().at(0);

		// check the total CO2 result: 13 kg
		assertEquals(13, resultC.getTotalFlowResult(co2Idx), 1e-10);

		// check sub-result B
		var techFlowB = TechFlow.of(sysB);
		var resultB = (FullResult) resultC.subResultOf(techFlowB);
		assertEquals(3, resultB.getTotalFlowResult(co2Idx), 1e-10);
		assertEquals(4, resultC.getScalingFactor(techFlowB), 1e-10);

		// check sub-result A
		var techFlowA = TechFlow.of(sysA);
		var resultA = (FullResult) resultB.subResultOf(techFlowA);
		assertEquals(1, resultA.getTotalFlowResult(co2Idx), 1e-10);
		assertEquals(2, resultB.getScalingFactor(techFlowA), 1e-10);

		// scale a sub-result to get the full result
		assertEquals(13, 1 + resultC.getScalingFactor(techFlowB)
				* resultB.getTotalFlowResult(co2Idx), 1e-10);

		// test TagResult
		var tagResults = TagResult.allOf(resultC);
		var expectedTagResults = List.of(
			Pair.of("A", 8.0),
			Pair.of("B", 4.0),
			Pair.of("C", 1.0));
		for (var expected : expectedTagResults) {
			var tagResult = tagResults.stream()
				.filter(r -> r.tag().equals(expected.first))
				.findFirst()
				.orElseThrow();
			assertEquals(
				expected.second, 
				tagResult.inventoryResultOf(co2Idx).value(), 1e-10);
		}

		db.delete(
			sysC, sysB, sysA,
			procA, procB, procC,
			prodA, prodB, prodC,
			co2, mass, units);
	}
}
