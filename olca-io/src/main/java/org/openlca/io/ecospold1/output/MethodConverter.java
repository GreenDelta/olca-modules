package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;

class MethodConverter {

	private final ImpactMethod method;
	private final EcoSpold1Config config;
	private final IEcoSpoldFactory factory = DataSetType.IMPACT_METHOD.getFactory();

	static IEcoSpold convert(ImpactMethod method, EcoSpold1Config config) {
		return new MethodConverter(method, config).doIt();
	}

	private MethodConverter(ImpactMethod method, EcoSpold1Config config) {
		this.method = method;
		this.config = config;
	}

	private IEcoSpold doIt() {
		var ecoSpold = factory.createEcoSpold();
		for (var category : method.impactCategories) {
			var ds = factory.createDataSet();
			var dataSet = new DataSet(ds, factory);
			Util.setDataSetAttributes(dataSet, method);
			mapLCIACategory(category, dataSet);
			IReferenceFunction refFun = dataSet.getReferenceFunction();
			refFun.setCategory(method.name);
			refFun.setGeneralComment(method.description);
			if (config.withDefaults) {
				SchemaDefaults.write(ds, factory);
			}
			ecoSpold.getDataset().add(ds);
		}
		return ecoSpold;
	}

	private void mapLCIACategory(ImpactCategory category, DataSet ds) {
		var refFun = factory.createReferenceFunction();
		ds.setReferenceFunction(refFun);
		String subCategory = category.name;
		String name = null;
		if (subCategory.contains("-")) {
			String[] parts = subCategory.split("-", 2);
			subCategory = parts[0].trim();
			name = parts[1].trim();
		}
		refFun.setSubCategory(subCategory);
		refFun.setName(name);
		refFun.setUnit(category.referenceUnit);
		for (ImpactFactor factor : category.impactFactors) {
			ds.getExchanges().add(mapLCIAFactor(factor));
		}
	}

	private IExchange mapLCIAFactor(ImpactFactor factor) {
		var exchange = factory.createExchange();
		Flow flow = factor.flow;
		exchange.setNumber((int) flow.id);
		Categories.map(factor.flow.category, exchange);
		Util.mapFlowInformation(exchange, factor.flow);
		exchange.setUnit(factor.unit.name);
		exchange.setName(factor.flow.name);
		exchange.setMeanValue(factor.value);
		return exchange;
	}

}
