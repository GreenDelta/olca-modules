package org.openlca.sd.eqn;

import java.util.List;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.util.Tensors;

/// Projects a tensor onto a different set of target dimensions.
///
/// In system dynamics, flows can have different dimensions than their
/// connected stocks. This class handles the dimension mismatch by projecting
/// tensors. Two projection modes are supported:
///
/// - **Same shape, different dimensions:** When the source tensor has the same
///   shape as the target dimensions (same number of elements per dimension),
///   values are copied directly to the corresponding target cells.
///
/// - **Multi-dimensional to 1D:** Sums an n-dimensional tensor down to a single
///   target dimension. The target dimension must exist in the source tensor.
///   Values are summed across all other dimensions.
///   E.g., source `[Product, Region]` with `flow[A,North]=1, flow[A,South]=2`
///   projected to `[Product]` gives `result[A]=3`
///
/// If the source tensor already has the same dimensions as the target,
/// it is returned unchanged.
public class TensorProjection {

	private final Tensor tensor;
	private final List<Dimension> dims;

	private TensorProjection(Tensor tensor, List<Dimension> dims) {
		this.tensor = tensor;
		this.dims = dims;
	}

	public static Res<Tensor> of(Tensor tensor, List<Dimension> dims) {
		if (tensor == null)
			return Res.error("No tensor provided");
		if (dims == null || dims.isEmpty())
			return Res.error("No target dimensions provided");
		if (eq(tensor.dimensions(), dims))
			return Res.ok(tensor);
		try {
			return new TensorProjection(tensor, dims).apply();
		} catch (Exception e) {
			return Res.error("Unexpected error in tensor projection", e);
		}
	}

	private static boolean eq(List<Dimension> sDims, List<Dimension> tDims) {
		if (sDims.size() != tDims.size())
			return false;
		for (int i = 0; i < sDims.size(); i++) {
			if (!Objects.equals(sDims.get(i), tDims.get(i)))
				return false;
		}
		return true;
	}

	private Res<Tensor> apply() {

		// when the source tensor has the same shape as the target tensor (but
		// different dimensions), just copy the values to the target cells
		if (sameShape()) {
			var target = Tensor.of(dims);
			var xs = Tensors.addressesOf(tensor);
			var ys = Tensors.addressesOf(target);
			for (int i = 0; i < xs.size(); i++) {
				target.set(ys.get(i), tensor.get(xs.get(i)));
			}
			return Res.ok(target);
		}

		// for different dimensions, we only allow summing up to a
		// single dimension.
		if (dims.size() != 1)
			return Res.error("Only one dimension is allowed as projection target");
		var dim = dims.getFirst();
		int idx = tensor.dimensions().indexOf(dim);
		if (idx < 0) {
			return Res.error(
				"Tensor does not contain the target dimension: " + dim.name());
		}

		var values = new double[dim.size()];
		for (var ax : Tensors.addressesOf(tensor)) {
			var ai = ax.get(idx);
			int i = dim.indexOf(ai);
			if (i < 0) {
				return Res.error("Dimension mismatch: " + ai);
			}
			values[i] += numOf(tensor.get(ax));
		}

		var target = Tensor.of(dim);
		for (int i = 0; i < values.length; i++) {
			target.set(i, Cell.of(values[i]));
		}
		return Res.ok(target);
	}

	private boolean sameShape() {
		var tDims = tensor.dimensions();
		if (tDims.size() != dims.size())
			return false;
		for (int i = 0; i < dims.size(); i++) {
			if (tDims.get(i).size() != dims.get(i).size())
				return false;
		}
		return true;
	}

	private double numOf(Cell cell) {
		return switch (cell) {
			case NumCell nc -> nc.value();
			case null, default -> 0.0;
		};
	}

}
