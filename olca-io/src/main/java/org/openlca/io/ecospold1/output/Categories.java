package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Category;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;

final class Categories {

	private Categories() {
	}

	static void map(Category category, IReferenceFunction refFun) {
		if (category == null || refFun == null)
			return;
		var parent = category.category;
		if (parent == null) {
			refFun.setCategory(category.name);
			refFun.setLocalCategory(category.name);
		} else {
			refFun.setCategory(parent.name);
			refFun.setLocalCategory(parent.name);
			refFun.setSubCategory(category.name);
			refFun.setLocalSubCategory(category.name);
		}
	}

	static void map(Category category, IExchange exchange) {
		if (category == null || exchange == null)
			return;
		if (category.category == null) {
			exchange.setCategory(category.name);
			exchange.setLocalCategory(category.name);
		} else {
			Category parent = category.category;
			exchange.setCategory(parent.name);
			exchange.setLocalCategory(parent.name);
			exchange.setSubCategory(category.name);
			exchange.setLocalSubCategory(category.name);
		}
	}
}
