package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public final class Auxil extends Var {

	public Auxil() {
		super();
		setValues(new ArrayList<>());
	}

	public Auxil(Id name, Cell def, String unit) {
		super(name, def, unit, new ArrayList<>());
	}

	@Override
	public Auxil freshCopy() {
		return new Auxil(name(), def(), unit());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Auxil auxil = (Auxil) o;
		return Objects.equals(name(), auxil.name());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}
}
