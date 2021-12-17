package org.openlca.ilcd.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Categories {

	private Categories() {
	}

	/**
	 * Returns the category path of the first classification system found in the
	 * given data set. When the data set is a flow data set it returns the
	 * category path of the compartments correspondingly.
	 */
	public static String[] getPath(IDataSet ds) {
		var path = new ArrayList<String>();
		Consumer<String> add = val -> {
			if (val == null)
				return;
			val = val.trim();
			if (val.isEmpty())
				return;
			path.add(val);
		};

		if (ds instanceof Flow flow) {
			compartments(flow).stream()
					.map(c -> c.value)
					.forEach(add);
			if (path.size() > 0)
				return path.toArray(new String[0]);
		}
		sorted(ds).stream()
				.map(c -> c.value)
				.forEach(add);
		return path.toArray(new String[0]);
	}

	private static List<Category> sorted(IDataSet ds) {
		if (ds == null)
			return Collections.emptyList();
		var list = ds.getClassifications();
		if (list.isEmpty())
			return Collections.emptyList();
		var classes = list.get(0).categories;
		classes.sort(Comparator.comparingInt(c -> c.level));
		return classes;
	}

	private static List<Compartment> compartments(Flow flow) {
		var info = Flows.getDataSetInfo(flow);
		if (info == null)
			return Collections.emptyList();
		var ci = info.classificationInformation;
		if (ci == null || ci.compartmentLists.isEmpty())
			return Collections.emptyList();
		var system = ci.compartmentLists.get(0);
		var compartments = system.compartments;
		compartments.sort(Comparator.comparingInt(c -> c.level));
		return compartments;
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
					if (reader.getLocalName()
							.equals("classificationInformation"))
						break;
					if (eq(reader, "classification")
							&& classification != null) {
						list.add(classification);
						classification = null;
					}
					if (eq(reader, "class") && category != null
							&& classification != null) {
						classification.categories.add(category);
						category = null;
					}
				}

				if (evt == XMLStreamConstants.CHARACTERS && category != null)
					addValue(reader, category);

			}

			reader.close();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Categories.class);
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
			Logger log = LoggerFactory.getLogger(Categories.class);
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
