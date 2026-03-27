package org.openlca.sd.eqn;

import org.junit.Test;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.SimSpecs;
import org.openlca.sd.model.cells.Cell;

import static org.junit.Assert.*;

public class TimeVarTest {

	@Test
	public void testTimeSeq() {

		var model = new SdModel();
		model.setSimSpecs(new SimSpecs(1, 5, 1, "Years"));
		model.vars().add(new Auxil(Id.of("t"), Cell.of("TIME"), null));

		var sim = Simulator.of(model.toXmile()).orElseThrow();
		int[] expected =  {1, 2, 3, 4, 5};
		int[] timeVar = new int[expected.length];
		int[] timeSim = new int[expected.length];

		int i = 0;
		for (var res : sim) {
			if (res.isError()) {
				fail(res.error());
				return;
			}

			var state = res.value();
			var value = state.valueOf(Id.of("t"));
			if (value.isEmpty()) {
				fail("Could not evaluate t");
				return;
			}

			timeVar[i] = (int) value.orElseThrow().asNum();
			timeSim[i] = (int) state.time();
			i++;
		}

		assertEquals(expected.length, i);
		assertArrayEquals(expected, timeVar);
		assertArrayEquals(expected, timeSim);
	}
}
