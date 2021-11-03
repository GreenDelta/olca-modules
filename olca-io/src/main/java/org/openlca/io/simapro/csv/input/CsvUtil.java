package org.openlca.io.simapro.csv.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.refdata.UnitRow;

class CsvUtil {

	private CsvUtil() {
	}

	static Set<String> allUnitsOf(CsvDataSet csv) {
		if (csv == null)
			return Collections.emptySet();
		var units = new HashSet<String>();

		// from unit rows
		for (var u : csv.units()) {
			units.add(u.name());
			units.add(u.referenceUnit());
		}

		// from flows
		for (var type : ElementaryFlowType.values()) {
			for (var f : csv.getElementaryFlows(type)) {
				units.add(f.unit());
			}
		}

		// from exchanges
		Consumer<List<? extends ExchangeRow>> exchanges = list -> {
			for (var e : list) {
				units.add(e.unit());
			}
		};
		for (var p: csv.processes()) {
			exchanges.accept(p.products());
			for (var type : ProductType.values()) {
				exchanges.accept(p.exchangesOf(type));
			}
			for (var type : ElementaryFlowType.values()) {
				exchanges.accept(p.exchangesOf(type));
			}
			if (p.wasteTreatment() != null) {
				exchanges.accept(List.of(p.wasteTreatment()));
			}
		}

		for (var s : csv.productStages()) {
			exchanges.accept(s.products());
			exchanges.accept(s.processes());
			exchanges.accept(s.additionalLifeCycles());
			exchanges.accept(s.disassemblies());
			exchanges.accept(s.materialsAndAssemblies());
			exchanges.accept(s.reuses());
			if (s.assembly() != null) {
				exchanges.accept(List.of(s.assembly()));
			}
			if (s.referenceAssembly() != null) {
				exchanges.accept(List.of(s.referenceAssembly()));
			}
		}

		// from LCIA factors
		for (var m : csv.methods()) {
			for (var i : m.impactCategories()) {
				for (var f : i.factors()) {
					units.add(f.unit());
				}
			}
		}

		return units;
	}

	static UnitRow unitRowOf(CsvDataSet ds, String unit) {
		if (ds == null || unit == null)
			return null;
		for (var row : ds.units()) {
			if (unit.equals(row.name()))
				return row;
		}
		return null;
	}

}
