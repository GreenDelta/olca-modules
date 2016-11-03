package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.IDataSet;

class ClassList {

	private ClassList() {
	}

	static List<Category> sortedList(IDataSet ds) {
		if (ds == null)
			return Collections.emptyList();
		List<Classification> list = ds.getClassifications();
		if (list.isEmpty())
			return Collections.emptyList();
		Classification classification = list.get(0);
		List<org.openlca.ilcd.commons.Category> classes = classification.categories;
		Collections.sort(classes, (c1, c2) -> c1.level - c2.level);
		return classes;
	}
}
