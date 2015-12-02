package org.openlca.core.math;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.model.Process;

public class CostTests {

	@Test
	public void test() {
		Process electricity = TestProcess.forOutput("Electricity", 1, "MJ").get();
	}

}
