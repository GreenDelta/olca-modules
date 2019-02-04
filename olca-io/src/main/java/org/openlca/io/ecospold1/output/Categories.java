package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;

final class Categories {

	private Categories() {
	}

	static void map(Flow flow, IReferenceFunction refFun, ExportConfig config) {
		Category category = flow.category;
		if (category != null) {
			if (category.category == null) {
				refFun.setCategory(category.name);
				refFun.setLocalCategory(category.name);
			} else {
				Category parent = category.category;
				refFun.setCategory(parent.name);
				refFun.setLocalCategory(parent.name);
				refFun.setSubCategory(category.name);
				refFun.setLocalSubCategory(category.name);
			}
		}
		if (config.isCreateDefaults())
			createDefaults(refFun);
	}

	private static void createDefaults(IReferenceFunction refFun) {
		if (refFun.getCategory() == null) {
			refFun.setCategory("unspecified");
			refFun.setLocalCategory("unspecified");
		}
		if (refFun.getSubCategory() == null) {
			refFun.setSubCategory("unspecified");
			refFun.setLocalSubCategory("unspecified");
		}
	}

	static void map(Category category, IExchange exchange, ExportConfig config) {
		if (category != null) {
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
		if (config.isCreateDefaults())
			createDefaults(exchange);
	}

	private static void createDefaults(IExchange exchange) {
		if (exchange.getCategory() == null) {
			exchange.setCategory("unspecified");
			exchange.setLocalCategory("unspecified");
		}
		if (exchange.getSubCategory() == null) {
			exchange.setSubCategory("unspecified");
			exchange.setLocalSubCategory("unspecified");
		}
	}
}
