package org.openlca.io.ecospold1.output;

import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;

class MethodConverter {

	private ImpactMethod method;
	private ExportConfig config;

	private IEcoSpoldFactory factory = DataSetType.IMPACT_METHOD.getFactory();

	static IEcoSpold convert(ImpactMethod method, ExportConfig config) {
		return new MethodConverter(method, config).doIt();
	}

	private MethodConverter(ImpactMethod method, ExportConfig config) {
		this.method = method;
		this.config = config;
	}

	private IEcoSpold doIt() {
		IEcoSpold ecoSpold = factory.createEcoSpold();
		for (ImpactCategory category : method.impactCategories) {
			IDataSet iDataSet = factory.createDataSet();
			DataSet dataSet = new DataSet(iDataSet, factory);
			Util.setDataSetAttributes(dataSet, method);
			mapLCIACategory(category, dataSet);
			IReferenceFunction refFun = dataSet.getReferenceFunction();
			refFun.setCategory(method.getName());
			refFun.setGeneralComment(method.getDescription());
			ecoSpold.getDataset().add(iDataSet);
		}
		return ecoSpold;
	}

	private void mapLCIACategory(ImpactCategory category, DataSet dataSet) {
		IReferenceFunction refFun = factory.createReferenceFunction();
		dataSet.setReferenceFunction(refFun);
		String subCategory = category.getName();
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
			dataSet.getExchanges().add(mapLCIAFactor(factor));
		}
	}

	private IExchange mapLCIAFactor(ImpactFactor factor) {
		IExchange exchange = factory.createExchange();
		Flow flow = factor.flow;
		exchange.setNumber((int) flow.getId());
		Categories.map(factor.flow.getCategory(), exchange, config);
		Util.mapFlowInformation(exchange, factor.flow);
		exchange.setUnit(factor.unit.getName());
		exchange.setName(factor.flow.getName());
		exchange.setMeanValue(factor.value);
		return exchange;
	}

}
