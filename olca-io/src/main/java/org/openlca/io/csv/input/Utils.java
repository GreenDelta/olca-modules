package org.openlca.io.csv.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.io.Categories;
import org.openlca.io.maps.CSVMapper;
import org.openlca.io.maps.content.CSVCategoryContent;
import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPParameter;

final class Utils {

	static Parameter create(SPParameter spParameter, ParameterScope scope) {
		Parameter parameter = new Parameter();
		parameter.setName(spParameter.getName());
		parameter.setScope(scope);
		if (nullCheck(spParameter.getComment()))
			parameter.setDescription(spParameter.getComment());

		if (spParameter instanceof SPInputParameter) {
			SPInputParameter inputParameter = (SPInputParameter) spParameter;
			parameter.setInputParameter(true);
			parameter.setValue(inputParameter.getValue());
		} else if (spParameter instanceof SPCalculatedParameter) {
			SPCalculatedParameter calculatedParameter = (SPCalculatedParameter) spParameter;
			parameter.setFormula(calculatedParameter.getExpression());
		}
		return parameter;
	}

	static boolean isNumeric(String value) {
		try {
			Double.parseDouble(value.replace(",", "."));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static Category createCategoryTree(IDatabase database, CSVMapper mapper,
			ModelType modelType, String categoryType, String category) {
		// TODO set default category
		CategoryDao dao = new CategoryDao(database);
		if (categoryType == null && category == null)
			return null;
		CSVCategoryContent content = mapper
				.getCategoryContentForImport(category);
		if (content != null && content.getOlcaRefId() != null) {
			Category c = dao.getForRefId(content.getOlcaRefId());
			if (c != null)
				return c;
		}
		List<String> list = new ArrayList<>();
		if (categoryType != null)
			list.add(categoryType);
		String categories[] = category.split("\\\\");
		for (String c : categories)
			list.add(c);
		return Categories.findOrAdd(database, modelType,
				list.toArray(new String[list.size()]));
	}

	/**
	 * 
	 * @param string
	 * @return false if the given string is null or equals "", otherwise true.
	 */
	static boolean nullCheck(String string) {
		if (string == null)
			return false;
		if (string.equals(""))
			return false;
		return true;
	}
}
