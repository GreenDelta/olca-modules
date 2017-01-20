package org.openlca.ilcd.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.IDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassList {

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

	/**
	 * Efficiently reads the list of classifications from a data set.
	 */
	public static List<Classification> read(InputStream is) {
		List<Classification> list = new ArrayList<>();
		try (BufferedInputStream buffer = new BufferedInputStream(is)) {
			XMLStreamReader reader = XMLInputFactory.newFactory()
					.createXMLStreamReader(buffer);

			Classification classification = null;
			Category category = null;
			while (reader.hasNext()) {
				int evt = reader.next();

				if (evt == XMLStreamConstants.START_ELEMENT) {
					if (eq(reader, "classification"))
						classification = initClassification(reader);
					if (eq(reader, "class") && classification != null)
						category = initCategory(reader);
				}

				if (evt == XMLStreamConstants.END_ELEMENT) {
					if (reader.getLocalName().equals("classificationInformation"))
						break;
					if (eq(reader, "classification") && classification != null) {
						list.add(classification);
						classification = null;
					}
					if (eq(reader, "class") && category != null && classification != null) {
						classification.categories.add(category);
						category = null;
					}
				}

				if (evt == XMLStreamConstants.CHARACTERS && category != null)
					addValue(reader, category);

			}

			reader.close();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ClassList.class);
			log.error("failed to read classifications", e);
		}
		return list;
	}

	private static Classification initClassification(XMLStreamReader reader) {
		Classification classification;
		classification = new Classification();
		classification.name = reader.getAttributeValue(null, "name");
		classification.url = reader.getAttributeValue(null, "classes");
		return classification;
	}

	private static Category initCategory(XMLStreamReader reader) {
		Category category;
		category = new Category();
		category.classId = reader.getAttributeValue(null, "classId");
		String level = reader.getAttributeValue(null, "level");
		if (level == null)
			return category;
		try {
			category.level = Integer.parseInt(level);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ClassList.class);
			log.error("No numeric level for class " + category.classId, e);
		}
		return category;
	}

	private static void addValue(XMLStreamReader reader, Category category) {
		String s = reader.getText();
		if (s == null)
			return;
		s = s.trim();
		if (s.isEmpty())
			return;
		if (category.value == null)
			category.value = s;
		else
			category.value += " " + s;
	}

	private static boolean eq(XMLStreamReader reader, String tag) {
		return reader.getLocalName().equals(tag);
	}

}
