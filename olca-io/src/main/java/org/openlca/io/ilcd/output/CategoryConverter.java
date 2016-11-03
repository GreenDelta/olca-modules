package org.openlca.io.ilcd.output;

import java.util.Stack;

import org.openlca.core.model.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;

class CategoryConverter {

	Classification getClassification(Category category) {
		if (category == null)
			return null;
		Classification classification = new Classification();
		if (category != null) {
			Stack<Category> stack = fillStack(category);
			makeClasses(classification, stack);
		}
		return classification;
	}

	CompartmentList getElementaryFlowCategory(Category category) {
		CompartmentList categorization = new CompartmentList();
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
			org.openlca.ilcd.commons.Category clazz = new org.openlca.ilcd.commons.Category();
			clazz.classId = category.getRefId();
			clazz.level = level;
			clazz.value = category.getName();
			classification.categories.add(clazz);
			level++;
		}
	}

	private void makeElementaryFlowCategories(CompartmentList list, Stack<Category> stack) {
		Category category;
		int level = 0;
		while (!stack.isEmpty()) {
			category = stack.pop();
			Compartment c = new Compartment();
			list.compartments.add(c);
			c.catId = category.getRefId();
			c.level = level;
			c.value = category.getName();
			level++;
		}
	}

}
