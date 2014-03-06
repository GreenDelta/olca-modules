package org.openlca.simapro.csv.model;

/**
 * A single data row in a CSV file.
 */
public interface IDataRow {

	void fill(String line, String separator);

}
