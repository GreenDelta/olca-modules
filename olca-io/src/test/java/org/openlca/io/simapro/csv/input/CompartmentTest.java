package org.openlca.io.simapro.csv.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;

public class CompartmentTest {

	@Test
	public void testNonMat() {
		assertEquals(
				ElementaryFlowType.NON_MATERIAL_EMISSIONS,
				ElementaryFlowType.of("Non mat."));
		assertEquals(
				SubCompartment.UNSPECIFIED,
				SubCompartment.of(""));
		assertEquals(
				SubCompartment.UNSPECIFIED,
				SubCompartment.of("(unspecified)"));
	}

	@Test
	public void testNoiseKey() {
		var quan = new UnitMappingEntry();
		quan.flowProperty = FlowProperty.of("mass*distance",
				UnitGroup.of("mass*distance", "tkm"));
		var processKey = FlowKey.elementary(
				Compartment.of(
						ElementaryFlowType.of("Non mat."),
						SubCompartment.of("")),
				"Noise, aircraft, freight",
				"tkm",
				quan);
		var impactKey = FlowKey.elementary(
				Compartment.of(
						ElementaryFlowType.of("Non mat."),
						SubCompartment.of(("(unspecified)"))),
				"Noise, aircraft, freight",
				"tkm",
				quan);

		assertEquals(processKey.mappingId(), impactKey.mappingId());
		assertEquals(processKey.refId(), impactKey.refId());
		assertEquals(processKey.type(), impactKey.type());
		assertEquals(processKey, impactKey);
	}

}
