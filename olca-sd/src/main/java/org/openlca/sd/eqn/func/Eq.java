package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Eq implements Func {

	private final Id name = Id.of("EQ");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Eq().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {

			// Both are numbers
			if (a.isNumCell() && b.isNumCell()) {
				boolean result = a.asNum() == b.asNum();
				return Res.ok(Cell.of(result));
			}

			// Both are booleans
			if (a.isBoolCell() && b.isBoolCell()) {
				boolean result = a.asBool() == b.asBool();
				return Res.ok(Cell.of(result));
			}

			// Both are empty
			if (a.isEmpty() && b.isEmpty()) {
				return Res.ok(Cell.of(true));
			}

			// Both are tensors - element-wise comparison
			if (a.isTensorCell() && b.isTensorCell()) {
				return compareTensors(a.asTensorCell(), b.asTensorCell());
			}

			// Number vs Tensor - special case: tensor = number is true when every cell equals the number
			if (a.isNumCell() && b.isTensorCell()) {
				return compareNumberToTensor(a.asNum(), b.asTensorCell());
			}
			if (a.isTensorCell() && b.isNumCell()) {
				return compareNumberToTensor(b.asNum(), a.asTensorCell());
			}

			// Different types are not equal
			return Res.ok(Cell.of(false));
		});
	}

	private Res<Cell> compareTensors(TensorCell cellA, TensorCell cellB) {
		var a = cellA.value();
		var b = cellB.value();

		// Check if shapes are the same
		var shapeA = a.shape();
		var shapeB = b.shape();
		if (shapeA.length != shapeB.length) {
			return Res.ok(Cell.of(false));
		}
		for (int i = 0; i < shapeA.length; i++) {
			if (shapeA[i] != shapeB[i]) {
				return Res.ok(Cell.of(false));
			}
		}

		// Compare elements recursively
		for (int i = 0; i < shapeA[0]; i++) {
			var ai = a.get(i);
			var bi = b.get(i);
			var comparison = apply(List.of(ai, bi));
			if (comparison.isError()) {
				return comparison.wrapError("error comparing tensor elements at index " + i);
			}
			if (!comparison.value().asBool()) {
				return Res.ok(Cell.of(false));
			}
		}
		return Res.ok(Cell.of(true));
	}

	private Res<Cell> compareNumberToTensor(double number, TensorCell tensorCell) {
		var tensor = tensorCell.value();
		var shape = tensor.shape();

		// Check if every element in the tensor equals the number (recursively)
		for (int i = 0; i < shape[0]; i++) {
			var element = tensor.get(i);
			var comparison = apply(List.of(Cell.of(number), element));
			if (comparison.isError()) {
				return comparison.wrapError("error comparing number to tensor element at index " + i);
			}
			if (!comparison.value().asBool()) {
				return Res.ok(Cell.of(false));
			}
		}
		return Res.ok(Cell.of(true));
	}
}
