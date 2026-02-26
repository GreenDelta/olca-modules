package org.openlca.sd.model.cells;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.model.Tensor;

/// The possible types of a tensor cell entry.
public sealed interface Cell permits
	EmptyCell,
	TensorCell,
	NumCell,
	BoolCell,
	EqnCell,
	LookupCell,
	LookupEqnCell,
	TensorEqnCell,
	NonNegativeCell {

	Res<Cell> eval(Interpreter interpreter);

	static Cell of(double num) {
		return new NumCell(num);
	}

	static Cell of(Tensor tensor) {
		return tensor != null
			? new TensorCell(tensor)
			: empty();
	}

	static Cell of(boolean b) {
		return new BoolCell(b);
	}

	static Cell of(String eqn) {
		return Id.isNil(eqn) ? Cell.empty() : new EqnCell(eqn);
	}

	static Cell empty() {
		return EmptyCell.get();
	}

	default boolean isEmpty() {
		return this instanceof EmptyCell;
	}

	default boolean isTensorCell() {
		return this instanceof TensorCell;
	}

	default boolean isNumCell() {
		return this instanceof NumCell;
	}

	default boolean isBoolCell() {
		return this instanceof BoolCell;
	}

	default boolean isEqnCell() {
		return this instanceof EqnCell;
	}

	default TensorCell asTensorCell() {
		if (this instanceof TensorCell cell)
			return cell;
		throw new IllegalStateException("is not a TensorCell");
	}

	default double asNum() {
		if (!(this instanceof NumCell(double  num)))
			throw new IllegalStateException("Not a numeric cell: " + this);
		return num;
	}

	default BoolCell asBoolCell() {
		if (this instanceof BoolCell cell)
			return cell;
		throw new IllegalStateException("is not a NumCell");
	}

	default boolean asBool() {
		return asBoolCell().value();
	}
}
