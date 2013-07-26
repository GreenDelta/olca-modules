/*******************************************************************************
 * Copyright (c) 2007 - 2012 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.math;

import java.util.Arrays;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.results.AnalysisResult;
import org.openlca.core.model.results.CalculationSetup;
import org.openlca.core.model.results.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the technology matrix & intervention matrix for a given product system
 */
public class MatrixSolver {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private IDatabase database;
	private AllocationMethod allocationMethod;
	private ImpactMethodDescriptor methodDescriptor;

	public MatrixSolver(IDatabase database) {
		this.database = database;
	}

	public void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public void setMethodDescriptor(ImpactMethodDescriptor methodDescriptor) {
		this.methodDescriptor = methodDescriptor;
	}

	public InventoryResult calculate(ProductSystem system) {
		InventoryMatrix matrix = buildMatrix(system);
		double[] g = MatrixMethod.solve(system, matrix);
		List<Exchange> exchanges = ExchangeResultList
				.on(database)
				.withReferenceFlow(system.getReferenceExchange().getFlow(),
						system.getConvertedTargetAmount())
				.create(matrix.getFlowIndex(), g);
		return createInventoryResult(system, exchanges);
	}

	private InventoryResult createInventoryResult(ProductSystem system,
			List<Exchange> inventory) {
		InventoryResult result = new InventoryResult();
		result.setProductSystemId(system.getRefId());
		result.setProductSystemName(system.getName());
		result.setProductName(system.getReferenceExchange().getFlow().getName());
		result.setUnitName(system.getTargetUnit().getName());
		result.setTargetAmount(system.getTargetAmount());
		result.getInventory().addAll(inventory);
		result.setCalculationMethod(getClass().getCanonicalName());
		return result;
	}

	private InventoryMatrix buildMatrix(ProductSystem system) {
		log.trace("build inventory matrix");
		InventoryMatrixBuilder builder = new InventoryMatrixBuilder(system);
		InventoryMatrix matrix = builder.build();
		AllocationMatrix.create(matrix, system, allocationMethod, database)
				.apply(matrix);
		log.trace("inventory matrix ready");
		return matrix;
	}

	public AnalysisResult analyse(ProductSystem productSystem) {

		InventoryMatrix matrix = buildMatrix(productSystem);
		CalculationSetup setup = new CalculationSetup();
		setup.setImpactMethod(methodDescriptor);
		setup.setProductSystem(productSystem);
		AnalysisResult results = new AnalysisResult(setup,
				matrix.getFlowIndex(), matrix.getProductIndex());
		if (matrix.isEmpty())
			return results;

		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		IMatrix inverse = techMatrix.getInverse();
		ProductIndex productIndex = matrix.getProductIndex();
		int n = productIndex.size();

		IMatrix demand = MatrixMethod.demandVector(productSystem, matrix);
		IMatrix scalingFactors = inverse.multiply(demand);
		results.setScalingFactors(scalingFactors.getColumn(0));

		// single results
		IMatrix scalingMatrix = MatrixFactory.create(n, n);
		for (int i = 0; i < n; i++) {
			scalingMatrix.setEntry(i, i, scalingFactors.getEntry(i, 0));
		}
		IMatrix singleResult = enviMatrix.multiply(scalingMatrix);
		results.setSingleResult(singleResult);

		// total results
		// TODO: loop correction
		IMatrix demandMatrix = MatrixFactory.create(n, n);
		for (int i = 0; i < productIndex.size(); i++) {
			Exchange product = productIndex.getProductAt(i);
			double amount = scalingFactors.getEntry(i, 0)
					* product.getConvertedResult();
			demandMatrix.setEntry(i, i, amount);
		}
		IMatrix totalResult = matrix.getInterventionMatrix().multiply(inverse)
				.multiply(demandMatrix);
		results.setTotalResult(totalResult);

		// Impact assessment results
		ImpactMatrix impactMatrix = buildImpactMatrix(matrix.getFlowIndex());
		if (impactMatrix != null) {
			List<ImpactCategoryDescriptor> impacts = Arrays.asList(impactMatrix
					.getCategoryIndex().getItems());
			setup.getImpactCategories().addAll(impacts);
			results.setImpactCategoryIndex(impactMatrix.getCategoryIndex());
			IMatrix factors = impactMatrix.getValues();
			results.setImpactFactors(factors);
			IMatrix singleImpactResult = factors.multiply(singleResult);
			results.setSingleImpactResult(singleImpactResult);
			IMatrix totalImpactResult = factors.multiply(totalResult);
			results.setTotalImpactResult(totalImpactResult);
		}

		return results;
	}

	private ImpactMatrix buildImpactMatrix(FlowIndex flowIndex) {
		if (methodDescriptor == null)
			return null;
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		ImpactMatrix impactMatrix = builder.build(methodDescriptor, flowIndex);
		return impactMatrix;
	}

}
