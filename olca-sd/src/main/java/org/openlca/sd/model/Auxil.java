package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public final class Auxil implements Var {

	private Id name;
	private Cell def;
	private String unit;
	private List<Cell> values;

	public Auxil() {
		this.values = new ArrayList<>();
	}

	public Auxil(Id name, Cell def, String unit) {
		this.name = Objects.requireNonNull(name);
		this.def = Objects.requireNonNull(def);
		this.unit = unit;
		this.values = new ArrayList<>();
	}

	public Auxil(Id name, Cell def, String unit, List<Cell> values) {
		this.name = Objects.requireNonNull(name);
		this.def = Objects.requireNonNull(def);
		this.unit = unit;
		this.values = Objects.requireNonNull(values);
	}

	@Override
	public Id name() {
		return name;
	}

	public void setName(Id name) {
		this.name = name;
	}

	@Override
	public Cell def() {
		return def;
	}

	public void setDef(Cell def) {
		this.def = def;
	}

	@Override
	public String unit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public List<Cell> values() {
		return values;
	}

	public void setValues(List<Cell> values) {
		this.values = values;
	}

	@Override
	public Auxil freshCopy() {
		return new Auxil(name, def, unit);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Auxil auxil = (Auxil) o;
		return Objects.equals(name, auxil.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
