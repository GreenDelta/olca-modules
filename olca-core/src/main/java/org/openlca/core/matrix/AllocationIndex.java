package org.openlca.core.matrix;

import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * An instances of this class provides fast access to the allocation factors
 * related to the products of a product system. It maps a product $p$ and an
 * exchange of flow $i$ to the respective allocation factor $\lambda_{p,i}$:
 * <p>
 * $$Idx_{Alloc}: (p, i) \mapsto \lambda_{p,i}$$
 */
public class AllocationIndex {

	/**
	 * Used for physical and economic allocation: directly stores the allocation
	 * factors for the given process-products.
	 */
	private HashMap<TechFlow, CalcAllocationFactor> factors;

	/**
	 * Used for causal allocation: stores the relation process-product -> exchange
	 * -> allocation factor.
	 */
	private HashMap<TechFlow, TLongObjectHashMap<CalcAllocationFactor>> causalFactors;

	public static AllocationIndex create(MatrixConfig config) {
		return create(config.db, config.techIndex, config.allocationMethod);
	}

	/**
	 * Creates a new allocation index for the given database, product index, and
	 * allocation method.
	 */
	public static AllocationIndex create(
			IDatabase db, TechIndex techIndex, AllocationMethod method) {
		var idx = new AllocationIndex();
		if (method == null || method == AllocationMethod.NONE)
			return idx;
		try {
			idx.build(db, techIndex, method);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(AllocationIndex.class);
			log.error("Failed to load allocation index", e);
		}
		return idx;
	}

	private AllocationIndex() {
	}

	/**
	 * Returns the value of the allocation factor $\lambda_{p,i}$ for the given
	 * product $p$ and (ID of the exchange with the) flow $i$. **It is very
	 * important** that this method is only called with exchanges that can be
	 * allocated to a product output or waste input, which are: product inputs,
	 * waste outputs, or elementary flows.
	 */
	public double get(TechFlow product, long exchangeID,
			FormulaInterpreter interpreter) {
		var factor = getFactor(product, exchangeID);
		return factor != null
				? factor.get(interpreter)
				: 1;
	}

	public CalcAllocationFactor getFactor(TechFlow product, long exchangeID) {
		if (product == null)
			return null;
		if (factors != null) {
			var factor = factors.get(product);
			if (factor != null)
				return factor;
		}
		if (causalFactors == null)
			return null;
		var causals = causalFactors.get(product);
		return causals == null
				? null
				: causals.get(exchangeID);
	}

	private void build(IDatabase db, TechIndex techIndex, AllocationMethod method) {

		// load process specific default allocation methods if required
		var defMethods = method == AllocationMethod.USE_DEFAULT
				? defaultMethods(db)
				: null;

		String sql = "SELECT " +
		/* 1 */ "allocation_type, " +
		/* 2 */ "f_process, " +
		/* 3 */ "f_product, " +
		/* 4 */ "f_exchange, " +
		/* 5 */ "value, " +
		/* 6 */ "formula " +
				"FROM tbl_allocation_factors";

		NativeSql.on(db).query(sql, r -> {
			long processID = r.getLong(2);

			// check if the allocation method matches
			AllocationMethod _method = method;
			if (defMethods != null) {
				_method = defMethods.get(processID);
			}
			String m = r.getString(1);
			if (m == null || AllocationMethod.valueOf(m) != _method)
				return true;

			// get the related product
			long productID = r.getLong(3);
			var product = techIndex.getProvider(processID, productID);
			if (product == null)
				return true;

			// index the factor
			var factor = CalcAllocationFactor.of(
					processID, r.getString(6), r.getDouble(5));
			if (_method != AllocationMethod.CAUSAL) {
				if (factors == null) {
					factors = new HashMap<>();
				}
				factors.put(product, factor);
				return true;
			}

			// causal allocation factors
			long exchangeID = r.getLong(4);
			if (causalFactors == null) {
				causalFactors = new HashMap<>();
			}
			var causals = causalFactors.get(product);
			if (causals == null) {
				causals = new TLongObjectHashMap<>();
				causalFactors.put(product, causals);
			}
			causals.put(exchangeID, factor);
			return true;
		});
	}

	private TLongObjectHashMap<AllocationMethod> defaultMethods(IDatabase db) {
		var methods = new TLongObjectHashMap<AllocationMethod>();
		String sql = "select id, default_allocation_method from tbl_processes";
		NativeSql.on(db).query(sql, r -> {
			long id = r.getLong(1);
			String method = r.getString(2);
			if (method != null) {
				methods.put(id, AllocationMethod.valueOf(method));
			}
			return true;
		});
		return methods;
	}

}
