package org.openlca.sd.eqn.func;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;

public class DivTest {

	@Test
	public void testScalarByTensor() {
		var dimX = Dimension.of("X", "a", "b");
		var dimY = Dimension.of("Y", "c", "d");
		var ti = Tensor.of(dimX, dimY);
		ti.setAll(7);

		var tr = Div.apply(Cell.of(42), Cell.of(ti))
			.orElseThrow()
			.asTensorCell()
			.value();

		assertEquals(dimX, tr.dimensions().getFirst());
		assertEquals(dimY, tr.dimensions().get(1));
		for (var x : dimX.elements()) {
			for (var y : dimY.elements()) {
				var val = tr.get(Subscript.of(x), Subscript.of(y)).asNum();
				assertEquals(6.0, val, 1e-16);
			}
		}
	}

	@Test
	public void testTensorByScalar() {
		var dimX = Dimension.of("X", "a", "b");
		var dimY = Dimension.of("Y", "c", "d");
		var ti = Tensor.of(dimX, dimY);
		ti.setAll(42);

		var tr = Div.apply(Cell.of(ti), Cell.of(7))
			.orElseThrow()
			.asTensorCell()
			.value();

		assertEquals(dimX, tr.dimensions().getFirst());
		assertEquals(dimY, tr.dimensions().get(1));
		for (var x : dimX.elements()) {
			for (var y : dimY.elements()) {
				var val = tr.get(Subscript.of(x), Subscript.of(y)).asNum();
				assertEquals(6.0, val, 1e-16);
			}
		}
	}

	@Test
	public void testElemWiseTensors() {
		var dimX = Dimension.of("X", "a", "b");
		var dimY = Dimension.of("Y", "c", "d");
		var ti = Tensor.of(dimX, dimY);
		ti.setAll(42);
		var tj = Tensor.of(dimX, dimY);
		tj.setAll(7);

		var tr = Div.apply(Cell.of(ti), Cell.of(tj))
			.orElseThrow()
			.asTensorCell()
			.value();

		assertEquals(dimX, tr.dimensions().getFirst());
		assertEquals(dimY, tr.dimensions().get(1));

		for (var x : dimX.elements()) {
			for (var y : dimY.elements()) {
				var val = tr.get(Subscript.of(x), Subscript.of(y)).asNum();
				assertEquals(6.0, val, 1e-16);
			}
		}
	}
}
