package org.openlca.util;

import static org.junit.Assert.*;

import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class FlowReplacerTest {

	private final IDatabase db = Tests.getDb();
	private FlowProperty mass;
	private FlowProperty area;

	@Before
	public void setup() {
		var massUnits = UnitGroup.of("Mass units", "kg");
		mass = FlowProperty.of("Mass", massUnits);
		var areaUnits = UnitGroup.of("Area units", "m2");
		area = FlowProperty.of("Area", areaUnits);
		db.insert(massUnits, areaUnits, mass, area);
	}

	@After
	public void cleanup() {
		db.delete(mass, mass.unitGroup);
	}

	@Test
	public void testFindUsedFlows() {
		var a = Flow.elementary("a", mass);
		var b = Flow.elementary("b", mass);
		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		P.output(a, 1);
		db.insert(a, b, p, P);

		var used = FlowReplacer.getUsedFlowsOf(db)
				.stream()
				.map(f -> f.refId)
				.collect(Collectors.toSet());
		db.delete(P, p, b, a);

		assertTrue(
				used.contains(a.refId)
						&& used.contains(p.refId)
						&& !used.contains(b.refId));
	}

	@Test
	public void testFindCandidates() {

		var a = Flow.elementary("a", mass);
		var b = Flow.elementary("b", area);
		b.flowPropertyFactors.add(FlowPropertyFactor.of(mass, 1000));
		var c = Flow.elementary("c", area);
		var p = Flow.product("d", mass);

		db.insert(a, b, c, p);
		var candidates = FlowReplacer.getCandidatesOf(db, Descriptor.of(a))
				.stream()
				.map(f -> f.refId)
				.collect(Collectors.toSet());
		db.delete(a, b, c, p);

		assertFalse(candidates.contains(a.refId));
		assertTrue(candidates.contains(b.refId));
		assertFalse(candidates.contains(c.refId));
		assertFalse(candidates.contains(p.refId));
	}

	@Test
	public void testReplaceInProcess() {
		var a = Flow.elementary("a", mass);
		var b = Flow.elementary("b", mass);
		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		P.version = 1L;
		P.output(a, 42);
		db.insert(a, b, p, P);

		FlowReplacer.of(db)
				.replaceIn(ModelType.PROCESS)
				.replace(Descriptor.of(a), Descriptor.of(b));

		P = db.get(Process.class, P.id);
		assertEquals(2L, P.version);
		var output = P.exchanges.stream()
				.filter(e -> e.amount == 42d)
				.findAny()
				.orElseThrow();
		assertEquals(b, output.flow);

		db.delete(P, p, b, a);
	}

	@Test
	public void testReplaceInImpacts() {
		var a = Flow.elementary("a", mass);
		var b = Flow.elementary("b", mass);
		var I = ImpactCategory.of("I");
		I.version = 1L;
		I.factor(a, 42);
		db.insert(a, b, I);

		FlowReplacer.of(db)
				.replaceIn(ModelType.IMPACT_CATEGORY)
				.replace(Descriptor.of(a), Descriptor.of(b));

		I = db.get(ImpactCategory.class, I.id);
		var cf = I.impactFactors.getFirst();
		assertEquals(b, cf.flow);
		assertEquals(2L, I.version);

		db.delete(I, b, a);
	}


}
