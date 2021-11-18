package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.FormulaConverter;
import org.openlca.util.Strings;

class ImportContext {

	private final IDatabase db;
	private final RefData refData;
	private final CsvDataSet dataSet;

	private ImportContext(Builder builder, CsvDataSet dataSet) {
		this.db = builder.db;
		this.refData = builder.refData;
		this.dataSet = dataSet;
	}

	IDatabase db() {
		return db;
	}

	RefData refData() {
		return refData;
	}

	public CsvDataSet dataSet() {
		return dataSet;
	}

	/**
	 * Converts a formula into syntactic form that can be understood by the
	 * openLCA formula interpreter.
	 */
	String convertFormula(String formula) {
		if (Strings.nullOrEmpty(formula))
			return null;
		return FormulaConverter.of(dataSet.header())
			.decimalSeparator('.')
			.parameterSeparator(';')
			.convert(formula);
	}

	static Builder of(IDatabase db, FlowMap flowMap) {
		var refData = new RefData(db, flowMap);
		return new Builder(db, refData);
	}

	record Builder (IDatabase db, RefData refData) {

		ImportContext next(CsvDataSet dataSet) {
			refData.sync(dataSet);
			return new ImportContext(this, dataSet);
		}
	}

}
