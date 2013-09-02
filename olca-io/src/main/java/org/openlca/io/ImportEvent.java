package org.openlca.io;

/**
 * An event that is thrown when a data set is imported.
 */
public class ImportEvent {

	private String dataSetName;

	public ImportEvent(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public String getDataSetName() {
		return dataSetName;
	}

}
