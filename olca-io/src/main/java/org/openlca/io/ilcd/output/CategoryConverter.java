package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.util.Stack;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.FlowCategorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CategoryConverter {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private java.lang.Class<?> modelClass;
	private IDatabase database;

	CategoryConverter(java.lang.Class<?> modelClass, IDatabase database) {
		this.modelClass = modelClass;
		this.database = database;
	}

	ClassificationInformation getClassificationInformation(String categoryId) {
		ClassificationInformation info = new ClassificationInformation();
		info.getClassifications().add(getClassification(categoryId));
		return info;
	}

	Classification getClassification(String categoryId) {
		Classification classification = new Classification();
		Category category = tryGetCategory(categoryId);
		if (category != null) {
			Stack<Category> stack = fillStack(category);
			makeClasses(classification, stack);
		}
		return classification;
	}

	FlowCategorization getElementaryFlowCategory(String categoryId) {
		FlowCategorization categorization = new FlowCategorization();
		Category category = tryGetCategory(categoryId);
		if (category != null) {
			Stack<Category> stack = fillStack(category);
			makeElementaryFlowCategories(categorization, stack);
		}
		return categorization;
	}

	private Category tryGetCategory(String categoryId) {
		try {
			return database.select(Category.class, categoryId);
		} catch (Exception e) {
			log.error("Cannot create classification for category id="
					+ categoryId, e);
			return null;
		}
	}

	private Stack<Category> fillStack(Category category) {
		Stack<Category> stack = new Stack<>();
		stack.push(category);
		while (isNonRoot(category)) {
			stack.push(category.getParentCategory());
			category = category.getParentCategory();
		}
		return stack;
	}

	private boolean isNonRoot(Category category) {
		return category.getParentCategory() != null
				&& category.getParentCategory().getId() != null
				&& !category.getParentCategory().getId().equals("root")
				&& !category.getParentCategory().getId()
						.equals(modelClass.getCanonicalName());
	}

	private void makeClasses(Classification classification,
			Stack<Category> stack) {
		Category category;
		int level = 0;
		while (!stack.isEmpty()) {
			category = stack.pop();
			org.openlca.ilcd.commons.Class clazz = new Class();
			clazz.setClassId(category.getId());
			clazz.setLevel(BigInteger.valueOf(level));
			clazz.setValue(category.getName());
			classification.getClasses().add(clazz);
			level++;
		}
	}

	private void makeElementaryFlowCategories(
			FlowCategorization categorization, Stack<Category> stack) {
		Category category;
		int level = 0;
		while (!stack.isEmpty()) {
			category = stack.pop();
			org.openlca.ilcd.commons.Category ilcdCategory = new org.openlca.ilcd.commons.Category();
			ilcdCategory.setCatId(category.getId());
			ilcdCategory.setLevel(BigInteger.valueOf(level));
			ilcdCategory.setValue(category.getName());
			categorization.getCategories().add(ilcdCategory);
			level++;
		}
	}

}
