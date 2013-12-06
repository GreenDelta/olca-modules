package org.openlca.simapro.csv.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

final class IndexEntry {

	static Object[] parse(Queue<String> lines, String csvSeperator)
			throws CSVParserException {
		Object[] entry = new Object[2];
		List<String> categories = new ArrayList<>();
		boolean noFinished = true;
		String[] split = null;
		while (!lines.isEmpty() && noFinished) {
			switch (lines.poll()) {
			case "Category type":
				ProcessCategory processCategory = ProcessCategory
						.forValue(lines.poll());
				if (processCategory == ProcessCategory.WASTE_SCENARIO)
					return null;
				categories.add(processCategory.getValue());
				break;
			case "Products":
				split = lines.poll().split(csvSeperator);
				addCategories(split[5], categories);
				entry[0] = split[0];
				noFinished = false;
				break;
			case "Waste treatment":
				split = lines.poll().split(csvSeperator);
				addCategories(split[4], categories);
				entry[0] = split[0];
				noFinished = false;
				break;
			}
		}
		entry[1] = categories.toArray(new String[categories.size()]);
		return entry;
	}

	private static void addCategories(String categories, List<String> list) {
		String vals[] = categories.split("\\\\");
		for (String c : vals)
			list.add(c);
	}
}
