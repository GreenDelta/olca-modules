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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.DataProviderException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.jobs.JobHandler;
import org.openlca.core.jobs.Jobs;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UncertaintyDistributionType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.results.LCIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the life cycle inventory of a product system
 * 
 * @see ILCICalculator
 * 
 * @author Sebastian Greve
 * 
 */
public class BaseSequentialCalculator implements ILCICalculator {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Indicates if loops should be calculated
	 */
	private boolean calculateLoops = false;

	/**
	 * The database
	 */
	private IDatabase database;

	/**
	 * The merged scaling factors
	 */
	private final List<ScalingFactor> factors = new ArrayList<>();

	/**
	 * flow id -> input/output
	 */
	private final Map<String, Boolean> flowIsInputMap = new HashMap<>();

	/**
	 * The job handler to report actual status and checking of user actions
	 * (e.g. if he canceled the operation)
	 */
	private final JobHandler handler = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);

	/**
	 * The maximum amount of iterations while scaling neighbours
	 */
	private int iterationMaximum = 1000;

	/**
	 * The limit of a scaling factor (if reached no more neighbours will be
	 * scaled in the actual recursion)
	 */
	private double lowerLimit = 1E-16;

	/**
	 * The product system to be calculated
	 */
	private ProductSystem system = null;

	/**
	 * Indicates if uncertainty values should be calculated
	 */
	private boolean uncertaintyCalculation;

	public BaseSequentialCalculator(Map<String, Object> properties) {
		initProperties(properties);
	}

	/**
	 * Aggregates the actual exchange with the before calculated results for the
	 * flow of the exchange
	 * 
	 * @param oldResult
	 *            The old result
	 * @param exchange
	 *            The actual exchange
	 * @param process
	 *            The process which holds the exchange
	 * @param productId
	 *            The id of the product for to which the scaling factor belongs
	 * @param productSystem
	 *            The product system to be calculated
	 * @param scalingFactor
	 *            The scaling factor of the actual process/product combination
	 * @return The aggregated result
	 * @throws DataProviderException
	 */
	private Exchange aggregateResult(Exchange oldResult,
			final Exchange exchange, final Process process,
			final String productId, final ProductSystem productSystem,
			final double scalingFactor) throws DataProviderException {
		// get the allocation factor
		final double allocationFactor = process.getAllocationFactor(exchange,
				productId);
		double resultingAmount = 0;
		// if reference exchange
		if (exchange.getFlow().getId()
				.equals(productSystem.getReferenceExchange().getFlow().getId())) {
			// amount * scaling factor * allocation factor
			resultingAmount = exchange.getResultingAmount().getValue()
					* scalingFactor * allocationFactor;
		} else {
			// converted amount * scaling factor * allocation factor
			resultingAmount = exchange.getConvertedResult() * scalingFactor
					* allocationFactor;
		}
		if (exchange.isInput()) {
			resultingAmount *= -1;
		}
		if (oldResult == null) {
			// create new exchange
			oldResult = new Exchange(productSystem.getId());
			oldResult.setFlow(exchange.getFlow());
			// get flow property factor
			final FlowPropertyFactor flowPropertyFactor = exchange.getFlow()
					.getFlowPropertyFactor(
							exchange.getFlow().getReferenceFlowProperty()
									.getId());

			oldResult.setFlowPropertyFactor(flowPropertyFactor);

			// load unit group
			final UnitGroup unitGroup = database.select(UnitGroup.class,
					exchange.getFlowPropertyFactor().getFlowProperty()
							.getUnitGroupId());
			oldResult.setUnit(unitGroup.getReferenceUnit());
			oldResult.setId(UUID.randomUUID().toString());
			oldResult.getResultingAmount().setValue(resultingAmount);
			oldResult.getResultingAmount().setFormula(
					Double.toString(oldResult.getResultingAmount().getValue()));
		} else {
			// append amount to existing exchange
			double amount = oldResult.getResultingAmount().getValue();
			amount += resultingAmount;
			oldResult.getResultingAmount().setValue(amount);
			oldResult.getResultingAmount().setFormula(Double.toString(amount));
		}
		return oldResult;
	}

	/**
	 * Calculates the standard deviations for each product/process combination
	 */
	private void calculateUncertainties() {
		// for each scaling factor
		for (final ScalingFactor factor : factors) {
			// uncertainty = sqrt(factor^2*factor uncertainty)
			factor.setUncertainty(Math.sqrt(Math.pow(factor.getFactor(), 2)
					* factor.getUncertainty()));
		}
	}

	/**
	 * Calculates the uncertainty for a LCI result
	 * 
	 * @param exchange
	 *            The actual exchange
	 * @param result
	 *            The aggregated result for the flow of the given exchange
	 * @param scalingFactor
	 *            The scaling factor of the actual exchange
	 * @param scalingFactorUncertainty
	 *            The standard deviation of the scaling factor of the actual
	 *            exchange
	 */
	private void calculateUncertaintyResult(final Exchange exchange,
			final Exchange result, final double scalingFactor,
			final double scalingFactorUncertainty) {
		// get the uncertainty of the exchange
		final double elemUnc = getUncertaintyValue(exchange);
		// calculate the uncertainty of the result
		// (resulting amount*scalingfactor uncertainty)^2 + (scalingfactor *
		// uncertainty of the exchange)^2
		final double uncertaintyValue = Math.pow(result.getResultingAmount()
				.getValue() * scalingFactorUncertainty, 2)
				+ Math.pow(scalingFactor * elemUnc, 2);
		if (uncertaintyValue != 0) {
			if (result.getDistributionType() != null
					&& result.getDistributionType() == UncertaintyDistributionType.NORMAL) {
				result.getUncertaintyParameter1().setValue(
						result.getResultingAmount().getValue());
				result.getUncertaintyParameter2().setValue(
						result.getUncertaintyParameter2().getValue()
								+ uncertaintyValue);
			} else {
				result.setDistributionType(UncertaintyDistributionType.NORMAL);
				result.getUncertaintyParameter1().setValue(
						result.getResultingAmount().getValue());
				result.getUncertaintyParameter2().setValue(uncertaintyValue);
			}
		}
	}

	/**
	 * Checks if a factor has reached the lower limit or is infinite
	 * 
	 * @param f
	 *            the factor to be checked
	 * @return true if the factor is lesser than the lower limit or NaN or
	 *         infinite, false otherwise
	 */
	private boolean checkError(final double f) {
		final boolean stop = Double.isInfinite(f) || Double.isNaN(f) || f == 0
				|| Math.abs(f) < lowerLimit;
		return stop;
	}

	/**
	 * Returns the scaling factor for a given link
	 * 
	 * @param link
	 *            The link the scaling factor is requested for
	 * @param factor
	 *            The old factor
	 * 
	 * @return the scaling factor for the given link
	 */
	private double getScalingFactorValue(final ProcessLink link,
			final ScalingFactor factor) {
		// get allocation factor
		final double allocationFactor = link.getRecipientProcess()
				.getAllocationFactor(link.getRecipientInput(),
						factor.getProductId());
		// calculate result
		double result = factor.getFactor()
				* link.getRecipientInput().getConvertedResult()
				* allocationFactor
				/ link.getProviderOutput().getConvertedResult();
		if (link.getRecipientInput().isAvoidedProduct()) {
			result *= -1;
		}
		return result;
	}

	/**
	 * Getter of the starting scaling factor
	 * 
	 * @return the first scaling factor
	 */
	private ScalingFactor getStartingFactor() {
		// get converted target amount
		final double f = system.getTargetAmount()
				/ system.getReferenceExchange().getConvertedResult()
				/ system.getTargetFlowPropertyFactor().getConversionFactor()
				* system.getTargetUnit().getConversionFactor();
		final ScalingFactor scalingFactor = new ScalingFactor();
		scalingFactor.setProcess(system.getReferenceProcess());
		scalingFactor.setProductId(system.getReferenceExchange().getId());
		scalingFactor.setFactor(f);
		scalingFactor.setUncertainty(0);
		return scalingFactor;
	}

	/**
	 * Getter of the standard deviation of a given exchange
	 * 
	 * @param exchange
	 *            The exchange the uncertainty value is requested for
	 * @return The standard deviation of the given exchange
	 */
	private double getUncertaintyValue(final Exchange exchange) {
		double value = 0;
		if (exchange.getDistributionType() != null
				&& exchange.getDistributionType() != UncertaintyDistributionType.NONE) {
			if (exchange.getDistributionType() == UncertaintyDistributionType.LOG_NORMAL) {
				value = Math
						.log(exchange.getUncertaintyParameter2().getValue());
			} else if (exchange.getDistributionType() == UncertaintyDistributionType.NORMAL) {
				value = exchange.getUncertaintyParameter2().getValue();
			}
		}
		return value;
	}

	/**
	 * Getter of the standard deviation of a process link
	 * 
	 * @param link
	 *            The link the uncertainty value is requested for
	 * @param factor
	 *            the scaling factor of the link
	 * @return the standard deviation of the allocated recipient input summed
	 *         with the standard deviation of the provider output
	 */
	private double getUncertaintyValue(final ProcessLink link,
			final ScalingFactor factor) {
		return getUncertaintyValue(link.getRecipientInput())
				* link.getRecipientProcess().getAllocationFactor(
						link.getRecipientInput(), factor.getProductId())
				+ getUncertaintyValue(link.getProviderOutput());
	}

	/**
	 * Initializes the properties from the properties map into the global fields
	 * 
	 * @param properties
	 *            The properties for the calculation
	 */
	private void initProperties(final Map<String, Object> properties) {
		if (properties.get("iterationMaximum") != null) {
			try {
				iterationMaximum = Integer.parseInt(properties.get(
						"iterationMaximum").toString());
			} catch (final NumberFormatException e) {
				// do nothing
			}
		}
		if (properties.get("lowerLimit") != null) {
			try {
				lowerLimit = Double.parseDouble(properties.get("lowerLimit")
						.toString());
			} catch (final NumberFormatException e) {
				// do nothing
			}
		}
		calculateLoops = properties.get("calculateLoops") != null
				&& properties.get("calculateLoops").toString().equals("true");
		uncertaintyCalculation = properties.get("uncertaintyCalculation") != null
				&& properties.get("uncertaintyCalculation").toString()
						.equals("true");
	}

	/**
	 * Looks in the product system if the given exchange is used in a process
	 * link
	 * 
	 * @param exchange
	 *            The exchange to look for
	 * @param productSystem
	 *            The product system to look in
	 * @return True if the exchange is used in a process link, false otherwise
	 */
	private boolean isLinkedProduct(final Exchange exchange,
			final ProductSystem productSystem) {
		boolean isLinkedProduct = false;
		// if product flow
		if (exchange.getFlow().getFlowType() == FlowType.ProductFlow) {
			int i = 0;
			final ProcessLink[] links = productSystem.getProcessLinks();
			// for each process link if no link was already found
			while (!isLinkedProduct && i < links.length) {
				// actual link to check
				final ProcessLink link = links[i];
				if (exchange.isInput()
						&& link.getRecipientInput().getId()
								.equals(exchange.getId())) {
					// if exchange is the input of the link && is input
					isLinkedProduct = true;
				} else if (!exchange.isInput()
						&& link.getProviderOutput().getId()
								.equals(exchange.getId())) {
					// if exchange is the output of the link && is output
					isLinkedProduct = true;
				}
				i++;
			}
		}
		return isLinkedProduct;
	}

	/**
	 * Merges the given scaling factors
	 * 
	 * @param newFactors
	 *            The list of scaling factors to be merged
	 */
	private void mergeFactors(final List<ScalingFactor> newFactors) {

		for (final ScalingFactor newFactor : newFactors) {

			boolean isNew = true;
			int i = 0;
			while (isNew && i < factors.size()) {
				final ScalingFactor factor = factors.get(i);
				if (factor.getProcess().getId()
						.equals(newFactor.getProcess().getId())
						&& factor.getProductId().equals(
								newFactor.getProductId())) {
					factor.setFactor(factor.getFactor() + newFactor.getFactor());
					newFactor.setFactor(factor.getFactor());
					factor.setUncertainty(factor.getUncertainty()
							+ newFactor.getUncertainty());
					newFactor.setUncertainty(factor.getUncertainty());
					isNew = false;
				} else {
					i++;
				}
			}

			if (isNew) {
				factors.add(newFactor);
			}

		}
	}

	/**
	 * Calculates the life cycle inventory of a product system
	 * 
	 * @param productSystem
	 *            The product system to be calculated
	 * @return A list of minimal exchanges representing the LCI result
	 * @throws DataProviderException
	 */
	private List<Exchange> result(final ProductSystem productSystem)
			throws DataProviderException {

		final Map<String, Exchange> exchanges = new HashMap<>();
		// for each scaling factor
		for (final ScalingFactor factor : factors) {
			// for each exchange of the factor's process
			for (final Exchange exchange : factor.process.getExchanges()) {
				flowIsInputMap.put(exchange.getFlow().getId(),
						exchange.isInput());
				// if the product is linked it does not have to be added
				// (scaled input + scaled output = 0)
				if (!isLinkedProduct(exchange, productSystem)
						|| exchange.getId().equals(
								productSystem.getReferenceExchange().getId())) {
					boolean stop = false;
					// if parent process is allocated and exchange is product
					// output but not the product the factor belongs to -> stop
					if (factor.process.getAllocationMethod() != null
							&& factor.process.getAllocationMethod() != AllocationMethod.None) {
						if (!exchange.isInput()
								&& exchange.getFlow().getFlowType() != FlowType.ElementaryFlow) {
							if (factor.process.getId()
									.equals(productSystem.getReferenceProcess()
											.getId())) {
								if (!exchange.getId().equals(
										productSystem.getReferenceExchange()
												.getId())) {
									stop = true;
								}
							} else if (!factor.productId.equals(exchange
									.getId())) {
								stop = true;
							}
						}
					}
					if (!stop) {
						// create exchange for result
						final Exchange result = aggregateResult(
								exchanges.get(exchange.getFlow().getId()),
								exchange, factor.getProcess(),
								factor.getProductId(), productSystem,
								factor.getFactor());
						if (exchange
								.getFlow()
								.getId()
								.equals(system.getReferenceExchange().getFlow()
										.getId())) {
							// if reference exchange
							// get the flow propertyfactpr of the reference flow
							// property
							final FlowPropertyFactor flowPropertyFactor = exchange
									.getFlow().getFlowPropertyFactor(
											exchange.getFlow()
													.getReferenceFlowProperty()
													.getId());

							exchange.setFlowPropertyFactor(flowPropertyFactor);

							// load unit group
							final UnitGroup unitGroup = database
									.select(UnitGroup.class, exchange
											.getFlowPropertyFactor()
											.getFlowProperty().getUnitGroupId());
							exchange.setUnit(unitGroup.getReferenceUnit());

							result.getResultingAmount().setValue(
									system.getConvertedTargetAmount());
						}
						result.getResultingAmount().setFormula(
								Double.toString(result.getResultingAmount()
										.getValue()));
						if (uncertaintyCalculation) {
							calculateUncertaintyResult(exchange, result,
									factor.getFactor(), factor.getUncertainty());
						}
						if (result.getResultingAmount().getValue() != 0) {
							exchanges.put(exchange.getFlow().getId(), result);
						} else {
							exchanges.remove(exchange.getFlow().getId());
						}
					}
				}
			}
		}
		final List<Exchange> resEx = new ArrayList<>();
		// for each exchange
		for (final Exchange exchange : exchanges.values()) {
			// set uncertainty
			boolean input = flowIsInputMap.get(exchange.getFlow().getId());
			if (uncertaintyCalculation
					&& exchange.getDistributionType() != null
					&& exchange.getDistributionType() == UncertaintyDistributionType.NORMAL) {
				final Expression meanValue = exchange
						.getUncertaintyParameter1();
				final Expression standardDeviation = exchange
						.getUncertaintyParameter2();
				double amount = meanValue.getValue();
				if (exchange.getFlow().getFlowType() == FlowType.ElementaryFlow) {
					amount *= input ? -1 : 1;
				} else {
					input = amount < 0;
					amount = Math.abs(amount);
				}
				final double sd = standardDeviation.getValue();
				meanValue.setValue(amount);
				standardDeviation.setValue(Math.sqrt(sd));
				meanValue.setFormula(Double.toString(amount));
				standardDeviation.setFormula(Double.toString(Math.sqrt(sd)));
				exchange.getResultingAmount().setValue(amount);
				exchange.getResultingAmount().setFormula(
						Double.toString(exchange.getResultingAmount()
								.getValue()));
				exchange.setInput(input);
			} else {
				double amount = exchange.getResultingAmount().getValue();
				if (exchange.getFlow().getFlowType() == FlowType.ElementaryFlow) {
					amount *= input ? -1 : 1;
				} else {
					input = amount < 0;
					amount = Math.abs(amount);
				}
				exchange.setInput(input);
				exchange.getResultingAmount().setValue(amount);
				exchange.getResultingAmount().setFormula(
						Double.toString(exchange.getResultingAmount()
								.getValue()));
			}
			resEx.add(exchange);
		}
		return resEx;
	}

	/**
	 * Scales the neighbours of a process/product combination
	 * 
	 * @param factor
	 *            the factor which contains the process/product combination to
	 *            be scaled
	 * @param iteration
	 *            recursion counter
	 * @param visitedProcesses
	 *            The processes that were already visited in early calls
	 */
	private void scaleNeighbors(final ScalingFactor factor,
			final int iteration, final List<String> visitedProcesses) {
		// if not user has canceled the operation
		if (!handler.jobIsCanceled()) {
			final List<ScalingFactor> nextFactors = new ArrayList<>();
			boolean stop = iteration >= iterationMaximum;
			final ProcessLink[] links = system.getIncomingLinks(factor
					.getProcess().getId());
			// for each link
			for (final ProcessLink link : links) {
				// get scaling factor of the link
				final double f = getScalingFactorValue(link, factor);

				stop = stop || checkError(f);

				final ScalingFactor nextFactor = new ScalingFactor();
				nextFactor.setProcess(link.getProviderProcess());
				nextFactor.setProductId(link.getProviderOutput().getId());
				nextFactor.setFactor(f);
				if (uncertaintyCalculation) {
					// calculate uncertainty value
					final double u = getUncertaintyValue(link, factor)
							+ factor.getUncertainty();
					nextFactor.setUncertainty(u);
				}
				nextFactors.add(nextFactor);
			}
			if (!stop) {
				for (final ScalingFactor nextFactor : nextFactors) {
					if (calculateLoops
							|| !visitedProcesses.contains(nextFactor
									.getProcess().getId())) {
						final List<String> newVisitedProcesses = new ArrayList<>();
						for (final String id : visitedProcesses) {
							newVisitedProcesses.add(id);
						}
						newVisitedProcesses
								.add(nextFactor.getProcess().getId());
						scaleNeighbors(nextFactor, iteration + 1,
								newVisitedProcesses);
						if (handler.jobIsCanceled()) {
							break;
						}
					}
				}
			}
			mergeFactors(nextFactors);
		}
	}

	@Override
	public LCIResult calculate(final ProductSystem productSystem,
			final IDatabase database) {
		system = productSystem;
		this.database = database;
		List<Exchange> result = new ArrayList<>();

		final ScalingFactor startFactor = getStartingFactor();
		if (uncertaintyCalculation) {
			startFactor.setUncertainty(getUncertaintyValue(system
					.getReferenceExchange()));
		}
		this.factors.add(startFactor);
		final List<String> visitedProcesses = new ArrayList<>();
		visitedProcesses.add(productSystem.getReferenceProcess().getId());
		// scale the processes
		scaleNeighbors(startFactor, 1, visitedProcesses);
		if (uncertaintyCalculation) {
			// calculate uncertainty values
			calculateUncertainties();
		}
		try {
			// calculate the result
			result = result(system);
		} catch (final Exception e) {
			log.error("Calculating result failed", e);
		}
		final List<org.openlca.core.model.ScalingFactor> factors = new ArrayList<>();
		for (final ScalingFactor f : this.factors) {
			// create scaling factor objects
			final org.openlca.core.model.ScalingFactor fac = new org.openlca.core.model.ScalingFactor();
			fac.setFactor(f.getFactor());
			fac.setId(UUID.randomUUID().toString());
			fac.setProcessId(f.getProcess().getId());
			fac.setProductId(f.getProductId());
			fac.setUncertainty(f.getUncertainty());
			factors.add(fac);
		}

		// create inventory
		LCIResult lciResult = new LCIResult();
		lciResult.setCalculationMethod(getClass().getCanonicalName());
		lciResult.getInventory().addAll(result);
		lciResult.setProductName(productSystem.getReferenceExchange().getFlow()
				.getName());
		lciResult.setProductSystemName(productSystem.getName());
		lciResult.setProductSystemId(productSystem.getId());
		// lciResult.setScalingFactors(factors);
		lciResult.setTargetAmount(productSystem.getTargetAmount());
		lciResult.setUnitName(productSystem.getTargetUnit().getName());

		this.factors.clear();
		this.system = null;
		final JobHandler handler = Jobs.getHandler(Jobs.MAIN_JOB_HANDLER);
		if (handler.jobIsCanceled())
			return null;
		return lciResult;
	}

	/**
	 * Internal scaling factor representation
	 * 
	 * @author Sebastian Greve
	 * 
	 */
	private class ScalingFactor {

		/**
		 * <p style="margin-top: 0">
		 * The factor
		 * </p>
		 */
		private double factor;

		/**
		 * <p style="margin-top: 0">
		 * The process the scaling factor belongs to
		 * </p>
		 */
		private Process process;

		/**
		 * <p style="margin-top: 0">
		 * The id of product exchange the scaling factor belongs to
		 * </p>
		 */
		private String productId;

		/**
		 * <p style="margin-top: 0">
		 * Uncertainty of the product
		 * </p>
		 */
		private double uncertainty;

		/**
		 * Getter of the factor
		 * 
		 * @return The scaling factor as double
		 */
		public double getFactor() {
			return factor;
		}

		/**
		 * Getter of the process
		 * 
		 * @return The process the scaling factor applies to
		 */
		public Process getProcess() {
			return process;
		}

		/**
		 * Getter of the product id
		 * 
		 * @return The id of the product the scaling factor applies to
		 */
		public String getProductId() {
			return productId;
		}

		/**
		 * Getter of the uncertainty
		 * 
		 * @return The uncertainty value
		 */
		public double getUncertainty() {
			return uncertainty;
		}

		/**
		 * Setter of the factor
		 * 
		 * @param factor
		 *            The scaling factor as double
		 */
		public void setFactor(final double factor) {
			this.factor = factor;
		}

		/**
		 * Setter of the process
		 * 
		 * @param process
		 *            The process the scaling factor applies to
		 */
		public void setProcess(final Process process) {
			this.process = process;
		}

		/**
		 * Setter of the product id
		 * 
		 * @param productId
		 *            The id of the product the scaling factor applies to
		 */
		public void setProductId(final String productId) {
			this.productId = productId;
		}

		/**
		 * Setter of the uncertainty
		 * 
		 * @param uncertainty
		 *            The uncertainty value
		 */
		public void setUncertainty(final double uncertainty) {
			this.uncertainty = uncertainty;
		}

	}
}
