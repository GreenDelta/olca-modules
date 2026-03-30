package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;

public class EvalOrderTest {

	@Test
	public void testMissingVariable() {
		var a = new Auxil(Id.of("a"), Cell.of("xyz + 1"));
		var r = EvaluationOrder.of(List.of(a));
		assertTrue(r.isError());
		var err = r.error();
		assertTrue(err.contains("xyz"));
		assertFalse(err.toLowerCase().contains("cycle"));
	}
}
