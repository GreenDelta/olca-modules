package org.openlca.sd.eqn.func;

import java.util.Arrays;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Add implements Func {

	private final Id name = Id.of("ADD");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Add().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {
			if (a.isEmpty())
				return Res.ok(b);
			if (b.isEmpty())
				return Res.ok(a);

			if (a.isNumCell() && b.isNumCell()) {
				double result = a.asNum() + b.asNum();
				return Res.ok(Cell.of(result));
			}

			if (a.isTensorCell() && b.isTensorCell())
				return add(a.asTensorCell(), b.asTensorCell());

			if (a.isNumCell() && b.isTensorCell())
				return add(a.asNum(), b.asTensorCell());

			if (a.isTensorCell() && b.isNumCell())
				return add(b.asNum(), a.asTensorCell());

			return Res.error("addition is not defined for: " + a + " + " + b);
		});
	}

	private Res<Cell> add(TensorCell cellA, TensorCell cellB) {
		var a = cellA.value();
		var b = cellB.value();

		var shapeA = a.shape();
		var shapeB = b.shape();

		if (!Arrays.equals(shapeA, shapeB)) {
			return Res.error("cannot add tensors with different shapes: " +
				Arrays.toString(shapeA) + " vs " + Arrays.toString(shapeB));
		}

		var sum = Tensor.of(a.dimensions());

		for (int i = 0; i < shapeA[0]; i++) {
			var ai = a.get(i);
			var bi = b.get(i);
			var si = apply(List.of(ai, bi));
			if (si.isError())
				return si.wrapError("error adding tensor elements at index " + i);
			sum.set(i, si.value());
		}
		return Res.ok(Cell.of(sum));
	}

	private Res<Cell> add(double scalar, TensorCell cellT) {
		var tensor = cellT.value();
		var shape = tensor.shape();
		var result = Tensor.of(tensor.dimensions());

		for (int i = 0; i < shape[0]; i++) {
			var element = tensor.get(i);
			var sum = apply(List.of(Cell.of(scalar), element));
			if (sum.isError())
				return sum.wrapError("error adding scalar to tensor element at index " + i);
			result.set(i, sum.value());
		}
		return Res.ok(Cell.of(result));
	}
}
