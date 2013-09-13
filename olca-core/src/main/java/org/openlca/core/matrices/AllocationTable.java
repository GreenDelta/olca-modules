package org.openlca.core.matrices;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the indices for the allocation factors of a product system. This
 * table should be only created if a system is build with an allocation method
 * (means method is not <code>null</code> or <code>NONE</code>).
 */
class AllocationTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProductIndex productIndex;
	private AllocationMethod method;

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

	public AllocationTable(IDatabase database, ProductIndex productIndex,
			AllocationMethod method) {
		this.productIndex = productIndex;
		this.method = method;
		init(database);
	}

	private void init(IDatabase database) {
		try (Connection con = database.createConnection()) {
			String query = createQuery();
			log.trace("fetch allocation factors: {}", query);
			ResultSet rs = con.createStatement().executeQuery(query);
			while (rs.next()) {
				CalcAllocationFactor factor = fetchFactor(rs);
				index(factor);
			}
			rs.close();
		} catch (Exception e) {
			log.error("failed to init allocation table", e);
		}
	}

	private String createQuery() {
		String sql = "select * from tbl_allocation_factors where ";
		if (method != AllocationMethod.USE_DEFAULT) {
			// we have do load all factors when the method is USE_DEFAULT,
			// otherwise we can filter the type via the query
			sql += "allocation_type = '" + method.name() + "' AND ";
		}
		sql += "f_process in " + Indices.asSql(productIndex.getProcessIds());
		return sql;
	}

	private CalcAllocationFactor fetchFactor(ResultSet rs) throws Exception {
		CalcAllocationFactor factor = new CalcAllocationFactor();
		String typeStr = rs.getString("allocation_type");
		factor.setMethod(AllocationMethod.valueOf(typeStr));
		factor.setProcessId(rs.getLong("f_process"));
		factor.setProductId(rs.getLong("f_product"));
		long exchangeId = rs.getLong("f_exchange");
		if (!rs.wasNull())
			factor.setExchangeId(exchangeId);
		return factor;
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
			return 1d; // TODO: this changes when we allow input-modelling of
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

	private double fetchCausal(LongPair processProduct,
			CalcExchange calcExchange) {
		if (exchangeFactors == null)
			return 1d;
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null)
			return 1d;
		return map.get(calcExchange.getExchangeId()); // default is 1.0
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

}
