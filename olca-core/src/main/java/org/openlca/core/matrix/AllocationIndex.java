package org.openlca.core.matrix;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.matrix.cache.AllocationTable;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

class AllocationIndex {

	private final AllocationMethod method;
	private final ProcessTable processTable;

	/**
	 * Used for physical and economic allocation: directly stores the the
	 * allocation factors for the given process-products.
	 */
	private HashMap<LongPair, Double> productFactors;

	/**
	 * Used for causal allocation: stores the relation process-product ->
	 * exchange -> allocation factor.
	 */
	private HashMap<LongPair, TLongDoubleHashMap> exchangeFactors;

	public static AllocationIndex create(TechIndex productIndex,
			AllocationMethod method, MatrixCache mCache) {
		return new AllocationIndex(productIndex, method, mCache);
	}

	private AllocationIndex(TechIndex index, AllocationMethod method,
			MatrixCache mCache) {
		this.method = method;
		List<CalcAllocationFactor> factors = AllocationTable.get(
				mCache.getDatabase(), index.getProcessIds());
		processTable = mCache.getProcessTable();
		for (CalcAllocationFactor f : factors) {
			index(f);
		}
	}

	private void index(CalcAllocationFactor f) {
		LongPair provider = new LongPair(f.processID, f.flowID);
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = processTable.getDefaultAllocationMethod(f.processID);
		if (_method == null)
			return;
		switch (_method) {
		case CAUSAL:
			tryIndexCausal(provider, f);
			break;
		case ECONOMIC:
			tryIndexForProduct(provider, f, _method);
			break;
		case PHYSICAL:
			tryIndexForProduct(provider, f, _method);
			break;
		default:
			break;
		}
	}

	private void tryIndexCausal(LongPair processProduct,
			CalcAllocationFactor factor) {
		if (factor.method != AllocationMethod.CAUSAL
				|| factor.exchangeID == null)
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
		map.put(factor.exchangeID, factor.value);
	}

	private void tryIndexForProduct(LongPair processProduct,
			CalcAllocationFactor factor, AllocationMethod method) {
		if (factor.method != method)
			return;
		if (method != AllocationMethod.ECONOMIC
				&& method != AllocationMethod.PHYSICAL)
			return;
		if (productFactors == null)
			productFactors = new HashMap<>();
		productFactors.put(processProduct, factor.value);
	}

	public double getFactor(LongPair provider, CalcExchange e) {
		if (!e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
			return 1d;
		if (e.isInput && e.flowType == FlowType.WASTE_FLOW)
			return 1d;
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = processTable.getDefaultAllocationMethod(
					provider.getFirst());
		if (_method == null)
			return 1d;
		switch (_method) {
		case CAUSAL:
			return causal(provider, e);
		case ECONOMIC:
			return forProvider(provider);
		case PHYSICAL:
			return forProvider(provider);
		default:
			return 1d;
		}
	}

	private double forProvider(LongPair provider) {
		if (productFactors == null)
			return 1d;
		Double factor = productFactors.get(provider);
		if (factor == null)
			return 1d;
		else
			return factor;
	}

	private double causal(LongPair provider, CalcExchange e) {
		if (exchangeFactors == null)
			return 1d;
		TLongDoubleHashMap map = exchangeFactors.get(provider);
		if (map == null)
			return 1d;
		return map.get(e.exchangeId); // default is 1.0
	}

}