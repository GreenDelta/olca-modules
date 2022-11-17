package org.openlca.io.refdata;

import org.apache.commons.csv.CSVRecord;

record CsvRow(CSVRecord record) {

	String get(int i) {
		return Csv.get(record, i);
	}

	double getDouble(int i) {
		return Csv.getDouble(record, i);
	}

	Double getOptionalDouble(int i) {
		return Csv.getOptionalDouble(record, i);
	}

}
