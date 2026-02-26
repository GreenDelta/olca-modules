package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Dot implements Func {

	private final Id name = Id.of("DOT");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Dot().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {

			// scalar multiplication (pass through to Mul)
			if (a.isNumCell() && b.isNumCell()) {
				return Mul.apply(a, b);
			}
			if (a.isNumCell() && b.isTensorCell()) {
				return Mul.apply(a, b);
			}
			if (a.isTensorCell() && b.isNumCell()) {
				return Mul.apply(a, b);
			}

			// matrix/vector multiplication:
			if (a.isTensorCell() && b.isTensorCell())
				return matrixMul(a.asTensorCell(), b.asTensorCell());

			return Res.error("dot product is not defined for: " + a + " · " + b);
		});
	}

	private Res<Cell> matrixMul(TensorCell cellA, TensorCell cellB) {
		var a = cellA.value();
		var b = cellB.value();
		var shapeA = a.shape();
		var shapeB = b.shape();

		// For matrix multiplication, we need compatible dimensions
		// A(m×n) × B(n×p) = C(m×p)
		// The inner dimensions must match: shapeA[1] == shapeB[0]

		if (shapeA.length == 1 && shapeB.length == 1) {
			// Vector dot product: both are 1D vectors of same length
			if (shapeA[0] != shapeB[0]) {
				return Res.error("cannot multiply vectors with different lengths: " +
					shapeA[0] + " vs " + shapeB[0]);
			}
			return dot(a, b);
		}

		if (shapeA.length == 2 && shapeB.length == 1) {
			// Matrix × vector: A(m×n) × B(n) = C(m)
			if (shapeA[1] != shapeB[0]) {
				return Res.error("cannot multiply matrix(" + shapeA[0] + "×" + shapeA[1] +
					") with vector(" + shapeB[0] + "): inner dimensions don't match");
			}
			return mv(a, b);
		}

		if (shapeA.length == 2 && shapeB.length == 2) {
			// Matrix × matrix: A(m×n) × B(n×p) = C(m×p)
			if (shapeA[1] != shapeB[0]) {
				return Res.error("cannot multiply matrices with incompatible dimensions: " +
					"(" + shapeA[0] + "×" + shapeA[1] + ") × (" + shapeB[0] + "×" + shapeB[1] + ")");
			}
			return mm(a, b);
		}

		return Res.error("dot product not supported for tensors with dimensions: " +
			shapeA.length + "D × " + shapeB.length + "D");
	}

	private Res<Cell> dot(Tensor a, Tensor b) {
		double sum = 0.0;
		var shape = a.shape();
		for (int i = 0; i < shape[0]; i++) {
			var ai = a.get(i);
			var bi = b.get(i);
			if (!ai.isNumCell() || !bi.isNumCell())
				return Res.error("vector dot product requires numeric elements");
			sum += ai.asNum() * bi.asNum();
		}
		return Res.ok(Cell.of(sum));
	}

	private Res<Cell> mv(Tensor m, Tensor v) {
		var matrixShape = m.shape();
		var result = new Tensor(m.dimensions().getFirst());
		for (int i = 0; i < matrixShape[0]; i++) {
			var row = m.get(i);
			if (!row.isTensorCell())
				return Res.error("matrix row is not a tensor at index " + i);
			var dot = dot(row.asTensorCell().value(), v);
			if (dot.isError())
				return dot.wrapError("error in matrix-vector multiplication at row " + i);
			result.set(i, dot.value());
		}
		return Res.ok(Cell.of(result));
	}

	private Res<Cell> mm(Tensor a, Tensor b) {
		var shapeA = a.shape();
		var shapeB = b.shape();
		var result = Tensor.of(
			List.of(a.dimensions().getFirst(), b.dimensions().get(1)));

		for (int i = 0; i < shapeA[0]; i++) {
			var rowA = a.get(i);
			if (!rowA.isTensorCell())
				return Res.error("row " + i + " of matrix A is not a tensor");
			var ai = rowA.asTensorCell().value();

			for (int j = 0; j < shapeB[1]; j++) {
				var colB = columnOf(b, j);
				if (colB.isError())
					return colB.wrapError("failed to get column " + j + " of matrix B");
				var cij = dot(ai, colB.value());
				if (cij.isError())
					return cij.wrapError(
						"failed to multiply row " + i + " with column " + j);
				result.get(i).asTensorCell().value().set(j, cij.value());
			}
		}
		return Res.ok(Cell.of(result));
	}

	private Res<Tensor> columnOf(Tensor matrix, int j) {
		var shape = matrix.shape();
		var col = new Tensor(matrix.dimensions().getFirst());
		for (int i = 0; i < shape[0]; i++) {
			var row = matrix.get(i);
			if (!row.isTensorCell())
				return Res.error("matrix row is not a tensor at index " + i);
			var element = row.asTensorCell().value().get(j);
			col.set(i, element);
		}
		return Res.ok(col);
	}
}
