package org.openlca.geo.lcia;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.UnitGroup;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GeoFactorMergeTest {

	private Flow flow1;
	private Flow flow2;
	private Flow flow3;
	private Location loca;
	private ImpactCategory impact;
	private List<ImpactFactor> newFactors;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		flow1 = Flow.elementary("f1", mass);
		flow2 = Flow.elementary("f2", mass);
		flow3 = Flow.elementary("f3", mass);
		loca = Location.of("LOC");

		impact = new ImpactCategory();
		impact.impactFactors.addAll(List.of(
				fa(flow1, null, 1),
				fa(flow1, loca, 2),
				fa(flow2, null, 3),
				fa(flow2, loca, 4)));

		var rand = ThreadLocalRandom.current();
		for (var e : List.of(units, mass, flow1, flow2, flow3, loca, impact)) {
			e.id = rand.nextLong();
		}

		newFactors = List.of(
				fa(flow2, null, 5),
				fa(flow2, loca, 6),
				fa(flow3, null, 7),
				fa(flow3, loca, 8));
	}

	@Test
	public void testKeepExisting() {
		GeoFactorMerge.keepExisting(impact).doIt(newFactors);
		expect(flow1, null, 1);
		expect(flow1, loca, 2);
		expect(flow2, null, 3);
		expect(flow2, loca, 4);
		expect(flow3, null, 7);
		expect(flow3, loca, 8);
	}

	@Test
	public void replaceExisting() {
		GeoFactorMerge.replaceExisting(impact).doIt(newFactors);
		expect(flow1, null, 1);
		expect(flow1, loca, 2);
		expect(flow2, null, 5);
		expect(flow2, loca, 6);
		expect(flow3, null, 7);
		expect(flow3, loca, 8);
	}

	private ImpactFactor fa(Flow flow, Location loc, double val) {
		var f = ImpactFactor.of(flow, val);
		f.location = loc;
		return f;
	}

	private void expect(Flow flow, Location loc, double val) {
		var debug = flow.name + (loc == null ? " - GLO" : loc.name);
		boolean found = false;
		for (var f : impact.impactFactors) {
			if (Objects.equals(f.flow, flow)
					&& Objects.equals(f.location, loc)) {
				assertEquals("wrong factor: " + debug, f.value, val, 1e-16);
				found = true;
				break;
			}
		}
		assertTrue("missing factor: " + debug, found);
	}
}
