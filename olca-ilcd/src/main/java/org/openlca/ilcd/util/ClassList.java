package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.ClassificationInfo;

class ClassList {

	private ClassList() {
	}

	static List<Class> sortedList(ClassificationInfo classInfo) {
		if (classInfo != null)
			return fromClassInfo(classInfo);
		return Collections.emptyList();
	}

	static List<Class> sortedList(Classification classification) {
		return fromClassification(classification);
	}

	private static List<Class> fromClassInfo(ClassificationInfo classInfo) {
		List<Classification> classifications = classInfo.classifications;
		if (classifications != null && classifications.size() > 0) {
			Classification classification = classifications.get(0);
			return fromClassification(classification);
		}
		return Collections.emptyList();
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
		Collections.sort(classes,
				new Comparator<org.openlca.ilcd.commons.Class>() {
					@Override
					public int compare(Class c1, Class c2) {
						int c = 0;
						if (c1.level != null && c2.level != null) {
							c = c1.level.compareTo(c2.level);
						}
						return c;
					}
				});
	}

}
