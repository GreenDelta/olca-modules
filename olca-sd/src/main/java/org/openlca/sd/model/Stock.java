package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.cells.Cell;

public final class Stock extends Var {

	private List<Id> inFlows;
	private List<Id> outFlows;

	public Stock() {
		super();
		this.inFlows = new ArrayList<>();
		this.outFlows = new ArrayList<>();
		setValues(new ArrayList<>());
	}

	public Stock(
		Id name, Cell def, String unit, List<Id> inFlows, List<Id> outFlows
	) {
		super(name, def, unit, new ArrayList<>());
		this.inFlows = Objects.requireNonNull(inFlows);
		this.outFlows = Objects.requireNonNull(outFlows);
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
	public Stock freshCopy() {
		return new Stock(
			name(), def(), unit(), new ArrayList<>(inFlows), new ArrayList<>(outFlows));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Stock stock = (Stock) o;
		return Objects.equals(name(), stock.name());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}
}
