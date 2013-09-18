package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

import java.util.HashMap;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

class AllocationIndex {

	/**
	 * Used for physical and economic allocation: directly stores the the
	 * allocation factors for the given process-products.
	 */
	private HashMap<LongPair, Double> productFactors;
	private ProductIndex productIndex;

	/**
	 * Used for causal allocation: stores the relation process-product ->
	 * exchange -> allocation factor.
	 */
	private HashMap<LongPair, TLongDoubleHashMap> exchangeFactors;

	private AllocationMethod method;

	public static AllocationIndex create(
			Iterable<CalcAllocationFactor> factors, ProductIndex productIndex,
			AllocationMethod method) {
		return new AllocationIndex(factors, productIndex, method);
	}

	private AllocationIndex(Iterable<CalcAllocationFactor> factors,
			ProductIndex productIndex, AllocationMethod method) {
		this.method = method;
		this.productIndex = productIndex;
		for (CalcAllocationFactor factor : factors)
			index(factor);
	}

	private void index(CalcAllocationFactor factor) {
		LongPair processProduct = new LongPair(factor.getProcessId(),
				factor.getProductId());
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = productIndex.getDefaultAllocationMethod(factor
					.getProcessId());
		if (_method == null)
			return;
		switch (_method) {
		case CAUSAL:
			tryIndexCausal(processProduct, factor);
			break;
		case ECONOMIC:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		case PHYSICAL:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		default:
			break;
		}
	}

	private void tryIndexCausal(LongPair processProduct,
			CalcAllocationFactor factor) {
		if (factor.getMethod() != AllocationMethod.CAUSAL
				|| factor.getExchangeId() == null)
			return;
		if (exchangeFactors == null)
			exchangeFactors = new HashMap<>();
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null) {
			// 1.0 is the default value -> means no allocation
			map = new TLongDoubleHashMap(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					Constants.DEFAULT_LONG_NO_ENTRY_VALUE, 1d);
			exchangeFactors.put(processProduct, map);
		}
		map.put(factor.getExchangeId(), factor.getValue());
	}

	private void tryIndexForProduct(LongPair processProduct,
			CalcAllocationFactor factor, AllocationMethod method) {
		if (factor.getMethod() != method)
			return;
		if (method != AllocationMethod.ECONOMIC
				&& method != AllocationMethod.PHYSICAL)
			return;
		if (productFactors == null)
			productFactors = new HashMap<>();
		productFactors.put(processProduct, factor.getValue());
	}

	public double getFactor(LongPair processProduct, CalcExchange calcExchange) {
		if (!calcExchange.isInput()
				&& calcExchange.getFlowType() == FlowType.PRODUCT_FLOW)
			return 1d; // TODO: this changes when we allow input-modelling
						// of
						// waste-flows
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = productIndex.getDefaultAllocationMethod(processProduct
					.getFirst());
		if (_method == null)
			return 1d;
		switch (_method) {
		case CAUSAL:
			return fetchCausal(processProduct, calcExchange);
		case ECONOMIC:
			return fetchForProduct(processProduct);
		case PHYSICAL:
			return fetchForProduct(processProduct);
		default:
			return 1d;
		}
	}

	private double fetchForProduct(LongPair processProduct) {
		if (productFactors == null)
			return 1d;
		Double factor = productFactors.get(processProduct);
		if (factor == null)
			return 1d;
		else
			return factor;
	}

	private double fetchCausal(LongPair processProduct,
			CalcExchange calcExchange) {
		if (exchangeFactors == null)
			return 1d;
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null)
			return 1d;
		return map.get(calcExchange.getExchangeId()); // default is 1.0
	}

}