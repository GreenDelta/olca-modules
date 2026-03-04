package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public final class Rate extends Var {

	public Rate() {
		super();
		setValues(new ArrayList<>());
	}

	public Rate(Id name, Cell def, String unit) {
		super(name, def, unit, new ArrayList<>());
	}

	public Rate(Id name, Cell def, String unit, List<Cell> values) {
		super(name, def, unit, Objects.requireNonNull(values));
	}

	@Override
	public Rate freshCopy() {
		return new Rate(name(), def(), unit());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rate rate = (Rate) o;
		return Objects.equals(name(), rate.name());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}
}
