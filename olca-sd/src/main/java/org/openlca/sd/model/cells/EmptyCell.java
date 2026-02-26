package org.openlca.sd.model.cells;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;

public record EmptyCell() implements Cell {
	private static final EmptyCell _instance = new EmptyCell();

	static EmptyCell get() {
		return _instance;
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		return Res.ok(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof EmptyCell;
	}

	@Override
	public String toString() {
		return "{}";
	}
}
