package org.openlca.io.ilcd.output;

import org.openlca.core.model.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Categories {

	private Categories() {
	}

	static void toClassification(
			Category category, Supplier<List<Classification>> fn
	) {
		if (category == null)
			return;
		var classification = new Classification();
		var stack = stackOf(category);
		int level = 0;
		while (!stack.isEmpty()) {
			var c = stack.pop();
			var clazz = new org.openlca.ilcd.commons.Category()
					.withClassId(c.refId)
					.withLevel(level)
					.withValue(c.name);
			classification.withCategories().add(clazz);
			level++;
		}
		fn.get().add(classification);
	}

	static Optional<CompartmentList> toCompartments(Category category) {
		if (category == null)
			return Optional.empty();
		var list = new CompartmentList();
		var stack = stackOf(category);
		int level = 0;
		while (!stack.isEmpty()) {
			var c = stack.pop();
			var comp = new Compartment()
					.withCatId(c.refId)
					.withLevel(level)
					.withName(c.name);
			list.withCompartments().add(comp);
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
