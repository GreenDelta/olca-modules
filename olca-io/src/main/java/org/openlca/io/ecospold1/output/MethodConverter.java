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
	private IEcoSpoldFactory factory = DataSetType.IMPACT_METHOD.getFactory();

	static IEcoSpold convert(ImpactMethod method) {
		return new MethodConverter(method).doIt();
	}

	private MethodConverter(ImpactMethod method) {
		this.method = method;
	}

	private IEcoSpold doIt() {
		IEcoSpold ecoSpold = factory.createEcoSpold();
		for (ImpactCategory category : method.getImpactCategories()) {
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
			name = subCategory.substring(subCategory.indexOf("-") + 1);
			while (name.startsWith(" ")) {
				name = name.substring(1);
			}
			subCategory = subCategory.substring(0, subCategory.indexOf("-"));
			while (subCategory.endsWith(" ")) {
				subCategory = subCategory
						.substring(0, subCategory.length() - 1);
			}
		}
		refFun.setSubCategory(subCategory);
		refFun.setName(name);
		refFun.setUnit(category.getReferenceUnit());
		for (ImpactFactor factor : category.getImpactFactors()) {
			dataSet.getExchanges().add(mapLCIAFactor(factor));
		}
	}

	private IExchange mapLCIAFactor(ImpactFactor factor) {
		IExchange exchange = factory.createExchange();
		Flow flow = factor.getFlow();
		exchange.setNumber((int) flow.getId());
		Util.mapFlowCategory(exchange, factor.getFlow().getCategory());
		Util.mapFlowInformation(exchange, factor.getFlow());
		exchange.setUnit(factor.getUnit().getName());
		exchange.setName(factor.getFlow().getName());
		exchange.setMeanValue(factor.getValue());
		return exchange;
	}

}
