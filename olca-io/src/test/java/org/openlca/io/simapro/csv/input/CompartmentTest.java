package org.openlca.io.simapro.csv.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
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
		var processKey = FlowKey.elementary(
			Compartment.of(
				ElementaryFlowType.of("Non mat."),
				SubCompartment.of("")),
			"Noise, aircraft, freight",
			"tkm");
		var impactKey = FlowKey.elementary(
			Compartment.of(
				ElementaryFlowType.of("Non mat."),
				SubCompartment.of(("(unspecified)"))),
			"Noise, aircraft, freight",
			"tkm");

		assertEquals(processKey.path(), impactKey.path());
		assertEquals(processKey.refId(), impactKey.refId());
		assertEquals(processKey.type(), impactKey.type());
		assertEquals(processKey, impactKey);
	}

}
