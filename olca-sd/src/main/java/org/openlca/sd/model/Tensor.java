package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.Subscript.Empty;
import org.openlca.sd.model.Subscript.Identifier;
import org.openlca.sd.model.Subscript.Index;
import org.openlca.sd.model.Subscript.Wildcard;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;

public class Tensor {

	private final Dimension dim;
	private final Cell[] cells;
	private final List<Dimension> subs;
	private final int n;

	private Tensor(Dimension dim, List<Dimension> subs) {
		this.dim = Objects.requireNonNull(dim);
		this.subs = Objects.requireNonNull(subs);
		this.cells = new Cell[dim.size()];
		this.n = 1 + subs.size();
		if (!subs.isEmpty()) {
			for (int i = 0; i < dim.size(); i++) {
				this.cells[i] = Cell.of(Tensor.of(subs));
			}
		}
	}

	public Tensor(Dimension dim) {
		this(dim, List.of());
	}

	public static Tensor of(List<Dimension> dims) {
		if (dims.isEmpty())
			throw new IllegalArgumentException(
				"Tensor must have at least one dimension");
		var primary = dims.getFirst();
		if (dims.size() == 1)
			return new Tensor(primary);

		var subs = new ArrayList<Dimension>(dims.size() - 1);
		for (int i = 1; i < dims.size(); i++) {
			subs.add(dims.get(i));
		}
		return new Tensor(primary, subs);
	}

	public static Tensor of(Dimension... dims) {
		return Tensor.of(Arrays.asList(dims));
	}

	/// Returns the sizes of the respective dimensions of this tensor.
	public int[] shape() {
		var shape = new int[n];
		shape[0] = dim.size();
		if (n == 1)
			return shape;
		for (int i = 0; i < subs.size(); i++) {
			shape[i + 1] = subs.get(i).size();
		}
		return shape;
	}

	/// Returns the size of the primary dimension of this tensor (the number
	/// of rows, as we store tensors row-oriented).
	public int size() {
		return cells.length;
	}

	public List<Dimension> dimensions() {
		var list = new ArrayList<Dimension>(n);
		list.add(dim);
		if (n > 1) {
			list.addAll(subs);
		}
		return list;
	}

	public void set(Subscript idx, Cell cell) {
		if (idx == null)
			return;
		switch (idx) {
			case Empty ignored -> {
			}
			case Wildcard ignored -> setAll(cell);
			case Index(int i) -> set(i, cell);
			case Identifier(Id id) -> {
				if (dim.hasName(id)) {
					setAll(cell);
				} else {
					set(dim.indexOf(id), cell);
				}
			}
		}
	}

	public void set(int index, Cell cell) {
		if (index < 0 || index >= dim.size())
			return;
		var c = cell != null ? cell : Cell.empty();
		cells[index] = c;
	}

	public void set(List<Subscript> subs, Cell cell) {
		if (subs == null || subs.isEmpty())
			return;

		var sub = subs.getFirst();
		if (subs.size() == 1 || n == 1) {
			set(sub, cell);
			return;
		}

		var rest = subs.subList(1, subs.size());

		if (isMatchAll(sub)) {
			for (var target : cells) {
				if (target instanceof TensorCell(Tensor tensor)) {
					tensor.set(rest, cell);
				}
			}
			return;
		}

		int idx = dim.indexOf(sub);
		if (idx < 0 || idx >= cells.length)
			return;
		if (cells[idx] instanceof TensorCell(Tensor tensor)) {
			tensor.set(rest, cell);
		}
	}

	public Cell get(Subscript sub) {
		if (isMatchAll(sub))
			return Cell.of(this);
		int i = dim.indexOf(sub);
		return get(i);
	}

	public Cell get(int index) {
		if (index < 0 || index >= dim.size())
			return Cell.empty();
		var cell = cells[index];
		return cell != null
			? cell
			: Cell.empty();
	}

	public Cell get(Subscript... subs) {
		return subs != null ? get(Arrays.asList(subs)) : Cell.empty();
	}

	public Cell get(List<Subscript> subs) {
		if (subs == null || subs.isEmpty())
			return Cell.empty();

		var sub = subs.getFirst();
		if (subs.size() == 1 || n == 1)
			return get(sub);

		var rest = subs.subList(1, subs.size());
		if (isMatchAll(sub)) {
			List<Dimension> innerDims = null;
			var cs = new Cell[dim.size()];
			for (int i = 0; i < cs.length; i++) {
				if (cells[i] instanceof TensorCell(Tensor queryTensor)) {
					var rs = queryTensor.get(rest);
					if (rs instanceof TensorCell(Tensor resultTensor)) {
						innerDims = resultTensor.dimensions();
					}
					cs[i] = rs;
				} else {
					cs[i] = Cell.empty();
				}
			}

			var outerResult = innerDims != null
				? new Tensor(dim, innerDims)
				: new Tensor(dim);
			System.arraycopy(cs, 0, outerResult.cells, 0, cs.length);
			return Cell.of(outerResult);
		}

		var idx = dim.indexOf(sub);
		if (idx < 0 || idx >= cells.length)
			return Cell.empty();

		if (cells[idx] instanceof TensorCell(Tensor tensor)) {
			return tensor.get(rest);
		}
		return Cell.empty();
	}

	public void setAll(Cell cell) {
		if (cell == null) {
			setAll(Cell.empty());
			return;
		}
		if (n == 1) {
			Arrays.fill(cells, cell);
			return;
		}
		for (var c : cells) {
			if (c instanceof TensorCell(Tensor tensor)) {
				tensor.setAll(cell);
			}
		}
	}

	public void setAll(double value) {
		setAll(new NumCell(value));
	}

	public Tensor copy() {
		var copy = new Tensor(dim, subs);
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] instanceof TensorCell(Tensor t)) {
				copy.cells[i] = Cell.of(t.copy());
			} else {
				// the other cell types are immutable
				copy.cells[i] = cells[i];
			}
		}
		return copy;
	}

	private boolean isMatchAll(Subscript s) {
		return switch (s) {
			case Wildcard ignored -> true;
			case Identifier(Id id) -> id.equals(dim.name());
			case null, default -> false;
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Tensor other))
			return false;

		// compare dimensions
		if (this.n != other.n || !Objects.equals(dim, other.dim))
			return false;

		for (int i = 0; i < subs.size(); i++) {
			if (!Objects.equals(subs.get(i), other.subs.get(i)))
				return false;
		}

		// compare cells
		for (int i = 0; i < cells.length; i++) {
			if (!Objects.equals(cells[i], other.cells[i]))
				return false;
		}
		return super.equals(obj);
	}
}
