package org.openlca.sd.eqn.func;

import java.util.Arrays;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Sub implements Func {

	private final Id name = Id.of("SUB");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Sub().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {
			if (b.isEmpty())
				return Res.ok(a);

			if (a.isNumCell() && b.isNumCell()) {
				double result = a.asNum() - b.asNum();
				return Res.ok(Cell.of(result));
			}

			if (a.isTensorCell() && b.isTensorCell())
				return sub(a.asTensorCell(), b.asTensorCell());

			if (a.isNumCell() && b.isTensorCell())
				return scalar(a.asNum(), b.asTensorCell());

			if (a.isTensorCell() && b.isNumCell())
				return scalar(a.asTensorCell(), b.asNum());

			return Res.error("subtraction is not defined for: " + a + " - " + b);
		});
	}

	private Res<Cell> sub(TensorCell cellA, TensorCell cellB) {
		var a = cellA.value();
		var b = cellB.value();

		var shapeA = a.shape();
		var shapeB = b.shape();

		if (!Arrays.equals(shapeA, shapeB)) {
			return Res.error("cannot subtract tensors with different shapes: " +
				Arrays.toString(shapeA) + " vs " + Arrays.toString(shapeB));
		}

		var diff = Tensor.of(a.dimensions());

		for (int i = 0; i < shapeA[0]; i++) {
			var ai = a.get(i);
			var bi = b.get(i);
			var di = apply(List.of(ai, bi));
			if (di.isError())
				return di.wrapError("error subtracting tensor elements at index " + i);
			diff.set(i, di.value());
		}
		return Res.ok(Cell.of(diff));
	}

	private Res<Cell> scalar(double scalar, TensorCell tensorCell) {
		var tensor = tensorCell.value();
		var shape = tensor.shape();
		var result = Tensor.of(tensor.dimensions());

		for (int i = 0; i < shape[0]; i++) {
			var element = tensor.get(i);
			var diff = apply(List.of(Cell.of(scalar), element));
			if (diff.isError())
				return diff.wrapError("error subtracting tensor element from scalar at index " + i);
			result.set(i, diff.value());
		}
		return Res.ok(Cell.of(result));
	}

	private Res<Cell> scalar(TensorCell tensorCell, double scalar) {
		var tensor = tensorCell.value();
		var shape = tensor.shape();
		var result = Tensor.of(tensor.dimensions());

		for (int i = 0; i < shape[0]; i++) {
			var element = tensor.get(i);
			var diff = apply(List.of(element, Cell.of(scalar)));
			if (diff.isError())
				return diff.wrapError("error subtracting scalar from tensor element at index " + i);
			result.set(i, diff.value());
		}
		return Res.ok(Cell.of(result));
	}

}
