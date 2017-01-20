package org.openlca.ilcd.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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
			JAXBContext context = JAXBContext.newInstance(Classification.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			XMLStreamReader reader = XMLInputFactory.newFactory()
					.createXMLStreamReader(buffer);
			while (reader.hasNext()) {
				int evt = reader.next();
				if (evt == XMLStreamConstants.END_ELEMENT) {
					if (reader.getLocalName().equals("classificationInformation"))
						break;
					continue;
				}
				if (evt != XMLStreamConstants.START_ELEMENT)
					continue;
				if (reader.getLocalName().equals("classification")) {
					Classification c = unmarshaller.unmarshal(reader,
							Classification.class).getValue();
					if (c != null)
						list.add(c);
				}
			}
			reader.close();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ClassList.class);
			log.error("failed to read classifications", e);
		}
		return list;
	}
}
