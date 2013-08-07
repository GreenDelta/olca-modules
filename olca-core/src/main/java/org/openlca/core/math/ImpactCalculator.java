/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.results.ImpactResult;
import org.openlca.core.model.results.InventoryResult;
import org.openlca.core.results.ImpactCategoryResult;

/**
 * Calculates an impact assessment result for an inventory result.
 */
public class ImpactCalculator {

	private IDatabase database;
	private InventoryResult inventoryResult;

	public ImpactCalculator(IDatabase database, InventoryResult inventoryResult) {
		this.database = database;
		this.inventoryResult = inventoryResult;
	}

	public ImpactResult calculate(ImpactMethodDescriptor method,
			NormalizationWeightingSet nwSet) {
		if (inventoryResult == null || inventoryResult.getInventory().isEmpty()
				|| method == null)
			return null;
		FlowIndex flowIndex = buildFlowIndex();
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		ImpactMatrix impactMatrix = builder.build(method, flowIndex);
		if (impactMatrix == null)
			return null;
		IMatrix factors = impactMatrix.getValues();
		IMatrix vector = makeVector(flowIndex);
		IMatrix result = factors.multiply(vector);
		Index<ImpactCategoryDescriptor> catIndex = impactMatrix
				.getCategoryIndex();
		ImpactResult impactResult = prepareResult(method, nwSet);
		impactResult.setLciaCategoryResults(fetchResults(result, catIndex,
				nwSet));
		return impactResult;
	}

	private ImpactResult prepareResult(ImpactMethodDescriptor method,
			NormalizationWeightingSet nwSet) {
		ImpactResult result = new ImpactResult();
		result.setRefId(UUID.randomUUID().toString());
		result.setLciaMethod(method.getName());
		result.setProduct(inventoryResult.getProductName());
		result.setProductSystem(inventoryResult.getProductSystemName());
		result.setTargetAmount(inventoryResult.getTargetAmount());
		if (nwSet != null) {
			result.setNormalizationWeightingSet(nwSet.getReferenceSystem());
			result.setWeightingUnit(nwSet.getUnit());
		}
		return result;
	}

	private List<ImpactCategoryResult> fetchResults(IMatrix result,
			Index<ImpactCategoryDescriptor> catIndex,
			NormalizationWeightingSet nwSet) {
		List<ImpactCategoryResult> resultList = new ArrayList<>(
				catIndex.size() + 2);
		for (ImpactCategoryDescriptor cat : catIndex.getItems()) {
			int row = catIndex.getIndex(cat);
			double val = result.getEntry(row, 0);
			ImpactCategoryResult r = new ImpactCategoryResult();
			r.setCategory(cat.getName());
			r.setUnit(cat.getReferenceUnit());
			r.setValue(val);
			if (nwSet != null) {
				addNwInfo(r, cat, nwSet);
			}
			resultList.add(r);
		}
		return resultList;
	}

	private FlowIndex buildFlowIndex() {
		FlowIndex index = new FlowIndex();
		for (Exchange exchange : inventoryResult.getInventory()) {
			index.put(exchange.getFlow());
			index.setInput(exchange.getFlow(), exchange.isInput());
		}
		return index;
	}

	private IMatrix makeVector(FlowIndex flowIndex) {
		IMatrix vector = MatrixFactory.create(flowIndex.size(), 1);
		for (Exchange exchange : inventoryResult.getInventory()) {
			Flow flow = exchange.getFlow();
			double val = exchange.getConvertedResult();
			if (flowIndex.isInput(flow))
				val = -val;
			int row = flowIndex.getIndex(flow);
			vector.setEntry(row, 0, val);
		}
		return vector;
	}

	private void addNwInfo(ImpactCategoryResult r, ImpactCategoryDescriptor cat,
			NormalizationWeightingSet nwSet) {
		NormalizationWeightingFactor factor = nwSet.getFactor(cat);
		if (factor == null)
			return;
		if (factor.getNormalizationFactor() != null)
			r.setNormalizationFactor(factor.getNormalizationFactor());
		if (factor.getWeightingFactor() != null)
			r.setWeightingFactor(factor.getWeightingFactor());
		r.setWeightingUnit(nwSet.getUnit());
	}

}
