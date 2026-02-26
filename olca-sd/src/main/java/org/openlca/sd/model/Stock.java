package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public final class Stock implements Var {

	private Id name;
	private Cell def;
	private String unit;
	private List<Id> inFlows;
	private List<Id> outFlows;
	private List<Cell> values;

	public Stock() {
		this.inFlows = new ArrayList<>();
		this.outFlows = new ArrayList<>();
		this.values = new ArrayList<>();
	}

	public Stock(Id name, Cell def, String unit, List<Id> inFlows, List<Id> outFlows) {
		this.name = Objects.requireNonNull(name);
		this.def = Objects.requireNonNull(def);
		this.unit = unit;
		this.inFlows = Objects.requireNonNull(inFlows);
		this.outFlows = Objects.requireNonNull(outFlows);
		this.values = new ArrayList<>();
	}

	public Stock(Id name, Cell def, String unit, List<Id> inFlows, List<Id> outFlows, List<Cell> values) {
		this.name = Objects.requireNonNull(name);
		this.def = Objects.requireNonNull(def);
		this.unit = unit;
		this.inFlows = Objects.requireNonNull(inFlows);
		this.outFlows = Objects.requireNonNull(outFlows);
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

	public List<Id> inFlows() {
		return inFlows;
	}

	public void setInFlows(List<Id> inFlows) {
		this.inFlows = inFlows;
	}

	public List<Id> outFlows() {
		return outFlows;
	}

	public void setOutFlows(List<Id> outFlows) {
		this.outFlows = outFlows;
	}

	@Override
	public List<Cell> values() {
		return values;
	}

	public void setValues(List<Cell> values) {
		this.values = values;
	}

	@Override
	public Stock freshCopy() {
		return new Stock(name, def, unit, new ArrayList<>(inFlows), new ArrayList<>(outFlows));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Stock stock = (Stock) o;
		return Objects.equals(name, stock.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
