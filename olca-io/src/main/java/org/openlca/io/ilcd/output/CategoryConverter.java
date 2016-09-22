package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.util.Stack;

import org.openlca.core.model.Category;
import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.FlowCategorization;

class CategoryConverter {

	ClassificationInfo getClassificationInformation(Category category) {
		ClassificationInfo info = new ClassificationInfo();
		info.classifications.add(getClassification(category));
		return info;
	}

	Classification getClassification(Category category) {
		Classification classification = new Classification();
		if (category != null) {
			Stack<Category> stack = fillStack(category);
			makeClasses(classification, stack);
		}
		return classification;
	}

	FlowCategorization getElementaryFlowCategory(Category category) {
		FlowCategorization categorization = new FlowCategorization();
		if (category != null) {
			Stack<Category> stack = fillStack(category);
			makeElementaryFlowCategories(categorization, stack);
		}
		return categorization;
	}

	private Stack<Category> fillStack(Category category) {
		Stack<Category> stack = new Stack<>();
		stack.push(category);
		while (isNonRoot(category)) {
			stack.push(category.getCategory());
			category = category.getCategory();
		}
		return stack;
	}

	private boolean isNonRoot(Category category) {
		return category.getCategory() != null;
	}

	private void makeClasses(Classification classification,
			Stack<Category> stack) {
		Category category;
		int level = 0;
		while (!stack.isEmpty()) {
			category = stack.pop();
			org.openlca.ilcd.commons.Class clazz = new Class();
			clazz.classId = category.getRefId();
			clazz.level = BigInteger.valueOf(level);
			clazz.value = category.getName();
			classification.classes.add(clazz);
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
			ilcdCategory.catId = category.getRefId();
			ilcdCategory.level = BigInteger.valueOf(level);
			ilcdCategory.value = category.getName();
			categorization.categories.add(ilcdCategory);
			level++;
		}
	}

}
