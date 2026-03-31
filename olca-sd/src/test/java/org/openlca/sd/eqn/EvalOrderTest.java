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

	@Test
	public void testBuiltInNames() {
		var a = new Auxil(Id.of("a"), Cell.of("PI + TIME + DT"));
		var r = EvaluationOrder.of(List.of(a));
		assertFalse(r.isError());
		assertEquals(1, r.value().size());
		assertEquals(Id.of("a"), r.value().getFirst().name());
	}

	@Test
	public void testCycle() {
		var a = new Auxil(Id.of("xy"), Cell.of("yx + 1"));
		var b = new Auxil(Id.of("yx"), Cell.of("xy + 1"));
		var r = EvaluationOrder.of(List.of(a, b));

		assertTrue(r.isError());
		var err = r.error().toLowerCase();
		assertTrue(err.contains("cycle"));
		assertTrue(err.contains("xy"));
		assertTrue(err.contains("yx"));
	}
}
