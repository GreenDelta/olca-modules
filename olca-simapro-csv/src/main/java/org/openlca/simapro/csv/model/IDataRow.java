package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;

/**
 * A single data row in a CSV file.
 */
public interface IDataRow {

	void fill(String line, CsvConfig config);

}
