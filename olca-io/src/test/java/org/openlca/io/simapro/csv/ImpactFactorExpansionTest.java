package org.openlca.io.simapro.csv;

import static org.junit.Assert.*;

import java.util.EnumMap;

import org.junit.Test;
import org.openlca.io.simapro.csv.input.ImpactFactors;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactFactorRow;

public class ImpactFactorExpansionTest {

	@Test
	public void testExpand() {

		var top = ElementaryFlowType.EMISSIONS_TO_AIR;
		var sub = SubCompartment.AIR_INDOOR;

		var impact = new ImpactCategoryBlock();
		impact.factors().add(new ImpactFactorRow()
			.compartment(top.compartment())
			.subCompartment(SubCompartment.UNSPECIFIED.toString())
			.factor(7)
			.flow("some flow")
			.unit("kg"));

		impact.factors().add(new ImpactFactorRow()
			.compartment(top.compartment())
			.subCompartment(sub.toString())
			.factor(42)
			.flow("some flow")
			.unit("kg"));

		ImpactFactors.expand(impact);

		// collect all factor values for all sub compartments
		var factors = new EnumMap<SubCompartment, Double>(SubCompartment.class);
		for (var row : impact.factors()) {
			assertEquals(top, ElementaryFlowType.of(row.compartment()));
			var subComp = SubCompartment.of(row.subCompartment());
			assertNull(factors.get(subComp));
			factors.put(subComp, row.factor());
		}

		int count = 0;
		for (var subComp : SubCompartment.values()) {
			if (subComp != SubCompartment.UNSPECIFIED && subComp.flowType() != top)
				continue;
			count++;
			var factor = factors.get(subComp);
			assertNotNull("missing factor for "
				+ subComp.flowType().compartment() + "/" + subComp, factor);
			if (subComp == sub) {
				assertEquals(42, factor, 1e-10);
			} else {
				assertEquals(7, factor, 1e-10);
			}
		}
		assertEquals(count, factors.size());
	}

}
