package org.openlca.io.ilcd.output;

import java.util.Optional;
import java.util.Stack;

import org.openlca.core.model.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;

class Categories {

	private Categories() {
	}

	static Optional<Classification> toClassification(Category category) {
		if (category == null)
			return Optional.empty();
		var classification = new Classification();
		var stack = stackOf(category);
		int level = 0;
		while (!stack.isEmpty()) {
			var c = stack.pop();
			var clazz = new org.openlca.ilcd.commons.Category();
			clazz.classId = c.refId;
			clazz.level = level;
			clazz.value = c.name;
			classification.categories.add(clazz);
			level++;
		}
		return Optional.of(classification);
	}

	static Optional<CompartmentList> toCompartments(Category category) {
		if (category == null)
			return Optional.empty();
		var list = new CompartmentList();
		var stack = stackOf(category);
		int level = 0;
		while (!stack.isEmpty()) {
			var c = stack.pop();
			var comp = new Compartment();
			list.compartments.add(comp);
			comp.catId = c.refId;
			comp.level = level;
			comp.value = c.name;
			level++;
		}
		return Optional.of(list);
	}

	private static Stack<Category> stackOf(Category category) {
		var stack = new Stack<Category>();
		stack.push(category);
		var c = category;
		while (c.category != null) {
			stack.push(c.category);
			c = c.category;
		}
		return stack;
	}

}
