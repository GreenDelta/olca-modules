package org.openlca.sd.model.cells;

import java.util.Objects;
import java.util.stream.Collectors;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.model.LookupFunc;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.util.Tensors;

/// A TensorEqnCell consists of a tensor and an equation. When the cell is
/// evaluated, first the equation is solved. Then the result of that equation
/// is applied on the tensor. For example, when the equation evaluates to a
/// number and the tensor contains lookup functions in its cells, the result of
/// the equation is applied to each lookup function of the cells to calculate
/// the respective cell values of the resulting tensor.
public record TensorEqnCell(Cell eqn, Tensor tensor) implements Cell {

	public TensorEqnCell {
		Objects.requireNonNull(eqn);
		Objects.requireNonNull(tensor);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		var res = eqn.eval(interpreter);
		if (res.isError()) return res;
		var result = res.value();

		if (result instanceof TensorCell(Tensor t)) {
			return apply(t, tensor);
		}
		if (!(result instanceof NumCell(double num))) {
			return Res.error(
				"Equation does not evaluate to a tensor or number: " + eqn);
		}
		return apply(num, tensor);
	}

	/// Applies the tensor calculated from the equation to the target tensor.
	private Res<Cell> apply(Tensor eqnRes, Tensor target) {
		if (!Tensors.haveSameDimensions(eqnRes, target)) {
			return Res.error("Equation result and tensor definition have " +
				"different dimensions; cast not supported");
		}

		var result = Tensor.of(target.dimensions());
		for (int i = 0; i < eqnRes.size(); i++) {
			var eqnCell = eqnRes.get(i);
			var targetCell = target.get(i);

			Res<Cell> resultCell = switch (eqnCell) {
				case TensorCell(Tensor fi) -> switch (targetCell) {
					case TensorCell(Tensor ii) -> apply(fi, ii);
					case TensorEqnCell(Cell ignore, Tensor ii) -> apply(fi, ii);
					case null, default -> null;
				};
				case NumCell(double num) -> apply(num, targetCell);
				case null, default -> null;
			};

			if (resultCell == null) {
				result.set(i, eqnCell);
				continue;
			}

			if (resultCell.isError()) {
				return resultCell.wrapError("Could not apply tensor row: " + i);
			}
			result.set(i, resultCell.value());
		}

		return Res.ok(new TensorCell(result));
	}

	private Res<Cell> apply(double value, Tensor tensor) {
		var t = Tensor.of(tensor.dimensions());
		for (int i = 0; i < tensor().size(); i++) {
			var entry = apply(value, tensor.get(i));
			if (entry.isError()) return entry;
			t.set(i, entry.value());
		}
		return Res.ok(new TensorCell(t));
	}

	private Res<Cell> apply(double value, Cell cell) {
		return switch (cell) {
			case LookupCell(LookupFunc func) -> apply(value, func);
			case LookupEqnCell(String ignore, LookupFunc func) -> apply(value, func);
			case TensorCell(Tensor t) -> apply(value, t);
			case null, default -> Res.ok(new NumCell(value));
		};
	}

	private Res<Cell> apply(double value, LookupFunc func) {
		double y = func.get(value);
		return Res.ok(new NumCell(y));
	}

	@Override
	public String toString() {
		var dims = tensor.dimensions()
			.stream()
			.map(d -> d.name().label())
			.collect(Collectors.joining(" × "));
		return "tensorEqn{" + dims + ",'" + eqn + "'}";
	}

}
