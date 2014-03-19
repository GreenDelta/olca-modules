package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.UnitRow;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains all reference data from a SimaPro CSV file.
 */
class SpRefDataIndex {

	private HashMap<String, Quantity> quantities = new HashMap<>();
	private HashSet<String> usedUnits = new HashSet<>();
	private HashMap<String, UnitRow> unitRows = new HashMap<>();

	private HashMap<String, ElementaryFlowRow> elementaryFlow = new HashMap<>();

	public void put(Quantity quantity) {
		if (quantity == null)
			return;
		quantities.put(quantity.getName(), quantity);
	}

	public Quantity getQuantity(String name) {
		return quantities.get(name);
	}

	public void put(UnitRow unitRow) {
		if (unitRow == null)
			return;
		String name = unitRow.getName();
		unitRows.put(name, unitRow);
	}

	public UnitRow getUnitRow(String name) {
		return unitRows.get(name);
	}

	public Collection<UnitRow> getUnitRows() {
		return unitRows.values();
	}

	public void putUsedUnit(String unitName) {
		if (unitName != null)
			usedUnits.add(unitName);
	}

	public Set<String> getUsedUnits() {
		return usedUnits;
	}
}
