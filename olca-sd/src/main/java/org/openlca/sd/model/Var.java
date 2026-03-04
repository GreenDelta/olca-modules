package org.openlca.sd.model;

import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public sealed abstract class Var permits Auxil, Rate, Stock {

	private Id name;
	private Cell def;
	private String unit;
	private List<Cell> values;

	protected Var() {
	}

	protected Var(Id name, Cell def, String unit, List<Cell> values) {
		this.name = Objects.requireNonNull(name);
		this.def = Objects.requireNonNull(def);
		this.unit = unit;
		this.values = Objects.requireNonNull(values);
	}

	public Id name() {
		return name;
	}

	public void setName(Id name) {
		this.name = name;
	}

	public Cell def() {
		return def;
	}

	public void setDef(Cell def) {
		this.def = def;
	}

	public String unit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public List<Cell> values() {
		return values;
	}

	public void setValues(List<Cell> values) {
		this.values = values;
	}

	/// Creates a fresh copy of the variable. This will not copy
	/// the values from the evaluation history.
	public abstract Var freshCopy();

	public void pushValue(Cell cell) {
		values().add(cell);
	}

	public Cell value() {
		return values().isEmpty()
				? def()
				: values().getLast();
	}

}
