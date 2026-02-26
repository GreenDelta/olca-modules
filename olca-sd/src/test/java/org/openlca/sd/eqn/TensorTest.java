package org.openlca.sd.eqn;

import static org.junit.Assert.*;
import static org.openlca.sd.model.Subscript.of;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;

public class TensorTest {

	@Test
	public void test1D() {
		var dim = Dimension.of("dim", "a", "b", "c");
		var t = Tensor.of(dim);
		t.setAll(42);
		assertEquals(1, t.dimensions().size());
		assertArrayEquals(new int[]{3}, t.shape());

		assertEquals(t, t.get(of("*")).asTensorCell().value());
		assertEquals(t, t.get(of("dim")).asTensorCell().value());

		for (var sub : List.of(of("a"), of("b"), of("c"))) {
			assertEquals(42, t.get(sub).asNum(), 1e-16);
		}

		t.set(Subscript.of("b"), Cell.of(21));
		assertEquals(21, t.get(of("b")).asNum(), 1e-16);
	}


	@Test
	public void test2D() {
		var dim1 = Dimension.of("Products", "PET", "PVC", "Nylon");
		var dim2 = Dimension.of("Location", "GLO", "US", "DE", "FR");
		var shares = Tensor.of(dim1, dim2);
		shares.set(of("PET, GLO"), Cell.of(0.3));
		shares.set(of("PVC, *"), Cell.of(0.1));
		shares.set(Subscript.of("Nylon"), Cell.of(0.2));
	}


}


