package org.openlca.io.ecospold2.input;

import java.util.Collections;
import java.util.List;

import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.Geography;
import org.openlca.ecospold2.Technology;
import org.openlca.ecospold2.TimePeriod;

class In {

	private In() {
	}

	static String id(DataSet ds) {
		Activity a = activity(ds);
		return a == null ? null : a.id;
	}

	static Activity activity(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.activity;
	}

	static Geography geography(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.geography;
	}

	static TimePeriod time(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.timePeriod;
	}

	static Technology technology(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.technology;
	}

	static List<Classification> classifications(DataSet ds) {
		if (ds == null || ds.description == null)
			return Collections.emptyList();
		return ds.description.classifications;
	}

}
