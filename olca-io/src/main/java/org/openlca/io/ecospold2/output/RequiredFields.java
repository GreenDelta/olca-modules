package org.openlca.io.ecospold2.output;

import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.Geography;

class RequiredFields {

	DataSet dataSet;

	void check(DataSet dataSet) {
		this.dataSet = dataSet;
		geography();
	}

	private void geography() {
		Geography geography = dataSet.getGeography();
		if (geography == null) {

		} else {
			if (geography.getShortName() == null
					|| geography.getShortName().equals("")) {
				
			}
		}
	}
}
