package org.openlca.core.math;

import org.junit.Test;
import org.openlca.core.TestProcess;

public class SimulatorTest {

	@Test
	public void testLCIAParam() {
		TestProcess.refProduct("p", 1.0, "kg")
				.elemOut("ch4", 1.0, "kg")
				.get();
	}

}
