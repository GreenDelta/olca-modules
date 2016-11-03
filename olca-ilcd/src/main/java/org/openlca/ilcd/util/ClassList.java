package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.Classification;

class ClassList {

	private ClassList() {
	}

	static List<Class> sortedList(Classification classification) {
		return fromClassification(classification);
	}

	private static List<Class> fromClassification(Classification classification) {
		List<org.openlca.ilcd.commons.Class> classes = classification.classes;
		if (classes != null && classes.size() > 0) {
			sortClasses(classes);
			return classes;
		}
		return Collections.emptyList();
	}

	private static void sortClasses(List<org.openlca.ilcd.commons.Class> classes) {
		Collections.sort(classes, (c1, c2) -> c1.level - c2.level);
	}

}
