package org.openlca.io.csv.input;

import java.util.List;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.SPParameter;

final class Utils {

//	static Category findCategory(String categoryName, ModelType modelType,
//			CategoryDao dao) throws Exception {
//		Category category = null;
//		for (Category c : dao.getForName(categoryName)) {
//
//			if (c.getModelType() == modelType) {
//				category = c;
//				break;
//			}
//		}
//		if (category != null)
//			return category;
//		category = new Category();
//		category.setRefId(UUID.randomUUID().toString());
//		category.setName(categoryName);
//		category.setModelType(modelType);
//		if (modelType == ModelType.FLOW) {
//			Category elemFlowCat = dao.getForRefId("elementary-flows");
//			if (elemFlowCat != null) {
//				elemFlowCat.add(category);
//				category.setParentCategory(elemFlowCat);
//				dao.update(elemFlowCat);
//				elemFlowCat.getChildCategories();
//			}
//		} else {
//			dao.insert(category);
//		}
//		return category;
//	}
//
//	static Category findSubCategory(String subCategoryName,
//			String parentCategoryName, ModelType modelType, CategoryDao dao)
//			throws Exception {
//		Category result = null;
//		for (Category c : dao.getForName(parentCategoryName)) {
//			if (c.getModelType() == modelType) {
//				for (Category subCategory : c.getChildCategories())
//					if (subCategory.getName().equals(subCategoryName)) {
//						result = subCategory;
//						break;
//					}
//			}
//		}
//		if (result != null)
//			return result;
//
//		Category parentCategory = findCategory(parentCategoryName, modelType,
//				dao);
//		Category subCategory = new Category();
//		subCategory.setRefId(UUID.randomUUID().toString());
//		subCategory.setModelType(modelType);
//		subCategory.setName(subCategoryName);
//		subCategory.setParentCategory(parentCategory);
//		parentCategory.add(subCategory);
//		dao.update(parentCategory);
//		result = subCategory;
//		return result;
//	}

	static Parameter convertInputParameter(SPInputParameter inputParameter,
			ParameterDao dao) throws Exception {
		Parameter parameter = null;
		List<Parameter> list = dao.getAllForName(inputParameter.getName(),
				ParameterScope.GLOBAL);
		if (!list.isEmpty())
			if (list.get(0).getValue() != inputParameter
					.getValue())
				// TODO: log not converted same parameter with another value
				// exist
				System.err.println("TODO log");
			else
				parameter = list.get(0);
		if (parameter == null) {
			parameter = convertParameter(inputParameter);
			if (inputParameter.getDistribution() != null) {
				// TODO convert
			}
			dao.insert(parameter);
		}
		return parameter;
	}

	static Parameter convertCalculatedParameter(
			SPCalculatedParameter calculatedParameter, ParameterDao dao)
			throws Exception {
		Parameter parameter = null;
		List<Parameter> list = dao.getAllForName(calculatedParameter.getName(),
				ParameterScope.GLOBAL);
		if (!list.isEmpty())
			if (list.get(0).getFormula() != calculatedParameter
					.getExpression())
				// TODO: log not converted same parameter with another value
				// exist
				System.err.println("TODO log");
			else
				parameter = list.get(0);
		if (parameter == null) {
			parameter = convertParameter(calculatedParameter);
			dao.insert(parameter);
		}
		return parameter;
	}

	private static Parameter convertParameter(SPParameter spParameter) {
		Parameter parameter = new Parameter();
		parameter.setName(spParameter.getName());
		if (!"".equals(spParameter.getComment()))
			parameter.setDescription(spParameter.getComment());
		parameter.setScope(ParameterScope.GLOBAL);
		return parameter;
	}

	static boolean compareComplete(String a, String b) {
		if (a != null && b != null)
			if (a.equals(b))
				return true;
			else
				return false;
		if (a == null && b == null)
			return true;
		if ("".equals(a) && b == null)
			return true;
		if ("".equals(a) && a == null)
			return true;
		else
			return false;
	}
}
