package org.openlca.simapro.csv.io;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.IDataRow;
import org.openlca.simapro.csv.model.Section;
import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.annotations.SectionRow;
import org.openlca.simapro.csv.model.annotations.SectionRows;
import org.openlca.simapro.csv.model.annotations.SectionValue;
import org.openlca.simapro.csv.model.enums.ValueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUnmarshaller {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final CsvConfig config;

	private Block block;
	private Object model;

	public BlockUnmarshaller(CsvConfig config) {
		this.config = config;
	}

	public <T> T unmarshall(Block block, Class<T> clazz) throws Exception {
		if (block == null || clazz == null)
			return null;
		log.trace("unmarshall block {} to class {}", block, clazz);
		T modelBlock = clazz.newInstance();
		this.model = modelBlock;
		this.block = block;
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(BlockRows.class))
				setBlockRows(field);
			if (field.isAnnotationPresent(SectionValue.class))
				setSectionValue(field);
			if (field.isAnnotationPresent(SectionRow.class))
				setSectionRow(field);
			if (field.isAnnotationPresent(SectionRows.class))
				setSectionRows(field);
		}
		this.model = null;
		this.block = null;
		return modelBlock;
	}

	private void setSectionRow(Field field) throws Exception {
		SectionRow sectionRow = field.getAnnotation(SectionRow.class);
		String sectionHeader = sectionRow.value();
		Section section = block.getSection(sectionHeader);
		if (section == null || section.dataRows.isEmpty())
			return;
		Class<?> type = field.getType();
		if (!(IDataRow.class.isAssignableFrom(type))) {
			logNoSectionList(field);
			return;
		}
		field.setAccessible(true);
		IDataRow instance = (IDataRow) type.newInstance();
		String row = section.dataRows.get(0);
		instance.fill(row, config);
		field.set(model, instance);
	}

	private void setBlockRows(Field field) {
		if (field == null)
			return;
		setDataRows(field, block.dataRows);
	}

	private void setSectionRows(Field field) {
		if (field == null)
			return;
		SectionRows rows = field.getAnnotation(SectionRows.class);
		Section section = block.getSection(rows.value());
		if (section == null || section.dataRows.isEmpty())
			return;
		setDataRows(field, section.dataRows);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setDataRows(Field field, List<String> lines) {
		try {
			field.setAccessible(true);
			List list = getListField(field);
			if (list == null)
				return;
			Class<?> argClass = getRowClass(field);
			if (argClass == null)
				return;
			for (String line : lines) {
				IDataRow row = (IDataRow) argClass.newInstance();
				row.fill(line, config);
				list.add(row);
			}
		} catch (Exception e) {
			log.error("failed to set data rows to field " + field, e);
		}
	}

	@SuppressWarnings("rawtypes")
	private List getListField(Field field) throws Exception {
		Object fieldContent = field.get(model);
		if (!(fieldContent instanceof List)) {
			logNoSectionList(field);
			return null;
		}
		List list = (List) fieldContent;
		return list;
	}

	private Class<?> getRowClass(Field field) {
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType)) {
			logNoSectionList(field);
			return null;
		}
		ParameterizedType pType = (ParameterizedType) type;
		Type[] typeArgs = pType.getActualTypeArguments();
		if (typeArgs == null || typeArgs.length != 1) {
			logNoSectionList(field);
			return null;
		}
		Type arg = typeArgs[0];
		if (!(arg instanceof Class)) {
			logNoSectionList(field);
			return null;
		}
		Class<?> argClass = (Class<?>) arg;
		if (!(IDataRow.class.isAssignableFrom(argClass))) {
			logNoSectionList(field);
			return null;
		}
		return argClass;
	}

	private void logNoSectionList(Field field) {
		log.error(
				"The field {} is not valid for section or block rows: it must be a"
						+ " live list with a declared type argument that is a "
						+ " class which implements IDataRow; e.g. List<Quantity>");
	}

	private void setSectionValue(Field field) {
		if (field == null)
			return;
		try {
			SectionValue sectionValue = field.getAnnotation(SectionValue.class);
			String sectionHeader = sectionValue.value();
			Section section = block.getSection(sectionHeader);
			if (section == null || section.dataRows.isEmpty())
				return;
			String val = section.dataRows.get(0);
			if (val.isEmpty())
				return;
			field.setAccessible(true);
			setFieldValue(field, val);
		} catch (Exception e) {
			log.error("failed to set value on field " + field, e);
		}
	}

	private void setFieldValue(Field field, String val)
			throws IllegalAccessException, Exception {
		Class<?> type = field.getType();
		if (type.equals(String.class))
			field.set(model, CsvUtils.strip(val));
		else if (type.equals(Boolean.class))
			setBooleanValue(field, val);
		else if (type.equals(Date.class))
			setDateValue(field, val);
		else if (ValueEnum.class.isAssignableFrom(type))
			setEnumValue(field, type, val);
		else
			log.error("at field: {} with value: {}; "
					+ "can only set section values "
					+ "to strings, booleans, dates and "
					+ "enumerations types that implement ValueEnum", field,
					val);
	}

	private void setEnumValue(Field field, Class<?> type, String val)
			throws Exception {
		if (val == null)
			return;
		for (Object o : type.getEnumConstants()) {
			if (!(o instanceof ValueEnum))
				continue;
			ValueEnum e = (ValueEnum) o;
			if (val.equalsIgnoreCase(e.getValue())) {
				field.set(model, e);
				return;
			}
		}
		log.error("did not find an enum value for {} at field {}", val, field);
	}

	private void setBooleanValue(Field field, String val) throws Exception {
		String lowerVal = val.toLowerCase();
		if (lowerVal.equals("yes") || lowerVal.equals(true)
				|| lowerVal.equals("1"))
			field.set(model, Boolean.TRUE);
		else
			field.set(model, Boolean.FALSE);
	}

	private void setDateValue(Field field, String val) throws Exception {
		if (config.getDateFormat() == null) {
			log.warn("no date-format given in CSV configuration; cannot set "
					+ "date values");
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat(config.getDateFormat());
		Date date = format.parse(val);
		field.set(model, date);
	}
}
