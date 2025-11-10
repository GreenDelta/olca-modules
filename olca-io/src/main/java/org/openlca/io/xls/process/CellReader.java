package org.openlca.io.xls.process;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.openlca.commons.Strings;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;

interface CellReader {

	default String str(Field field) {
		return In.stringOf(cellOf(field));
	}

	default Date date(Field field) {
		return In.dateOf(cellOf(field));
	}

	default double num(Field field) {
		return In.doubleOf(cellOf(field));
	}

	default boolean bool(Field field) {
		return In.booleanOf(cellOf(field));
	}

	default Object value(Field field) {
		return In.valueOf(cellOf(field));
	}

	default Uncertainty uncertainty() {
		var type = str(Field.UNCERTAINTY);
		if (Strings.isBlank(type))
			return null;
		return switch (type.trim().toLowerCase()) {
			case "normal" -> Uncertainty.normal(
				num(Field.MEAN_MODE), num(Field.SD));
			case "log-normal" -> Uncertainty.logNormal(
				num(Field.MEAN_MODE), num(Field.SD));
			case "triangular" -> Uncertainty.triangle(
				num(Field.MINIMUM), num(Field.MEAN_MODE), num(Field.MAXIMUM));
			case "uniform" -> Uncertainty.uniform(
				num(Field.MINIMUM), num(Field.MAXIMUM));
			default -> null;
		};
	}

	default <T extends RootEntity> T get(
		Field field, InConfig config, Class<T> type) {
		var name = str(field);
		if (Strings.isBlank(name))
			return null;
		return config.index().get(type, name);
	}

	default Category syncCategory(IDatabase db, ModelType type) {
		var path = str(Field.CATEGORY);
		if (Strings.isBlank(path))
			return null;
		var parts = path.split("/");
		return CategoryDao.sync(db, type, parts);
	}

	Cell cellOf(Field field);

}
