package org.openlca.simapro.csv.reader;

import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.IDataRow;
import org.openlca.simapro.csv.model.Section;
import org.openlca.simapro.csv.model.annotations.SectionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockUnmarshaller {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Block block;
	private Object model;

	public <T extends IDataRow> List<T> unmarshallRows(Block block, Class<T> clazz,
			String separator) throws Exception {
		if (block == null || clazz == null || block.getDataRows().isEmpty())
			return Collections.emptyList();
		List<T> results = new ArrayList<>();
		for (String row : block.getDataRows()) {
			T instance = clazz.newInstance();
			instance.fill(row, separator);
			results.add(instance);
		}
		return results;
	}

	public <T> T unmarshall(Block block, Class<T> clazz) throws Exception {
		if (block == null || clazz == null)
			return null;
		log.trace("unmarshall block {} to class {}", block, clazz);
		T modelBlock = clazz.newInstance();
		this.model = modelBlock;
		this.block = block;
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(SectionValue.class))
				setSectionValue(field);
		}
		return modelBlock;
	}

	private void setSectionValue(Field field) {
		if (field == null)
			return;
		try {
			SectionValue sectionValue = field.getAnnotation(SectionValue.class);
			String sectionHeader = sectionValue.value();
			Section section = block.getSection(sectionHeader);
			if (section == null || section.getDataRows().isEmpty())
				return;
			String val = section.getDataRows().get(0);
			field.setAccessible(true);
			field.set(model, val);
		} catch (Exception e) {
			log.error("failed to set value on field " + field, e);
		}
	}
}
