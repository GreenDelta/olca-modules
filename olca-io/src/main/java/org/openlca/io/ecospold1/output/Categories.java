package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;

final class Categories {

	private Categories() {
	}

	static void map(Flow flow, IReferenceFunction refFun, ExportConfig config) {
		Category category = flow.getCategory();
		if (category != null) {
			if (category.getCategory() == null) {
				refFun.setCategory(category.getName());
				refFun.setLocalCategory(category.getName());
			} else {
				Category parent = category.getCategory();
				refFun.setCategory(parent.getName());
				refFun.setLocalCategory(parent.getName());
				refFun.setSubCategory(category.getName());
				refFun.setLocalSubCategory(category.getName());
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
			if (category.getCategory() == null) {
				exchange.setCategory(category.getName());
				exchange.setLocalCategory(category.getName());
			} else {
				Category parent = category.getCategory();
				exchange.setCategory(parent.getName());
				exchange.setLocalCategory(parent.getName());
				exchange.setSubCategory(category.getName());
				exchange.setLocalSubCategory(category.getName());
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
