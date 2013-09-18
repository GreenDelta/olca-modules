package org.openlca.core.matrix.cache;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;

import com.google.common.cache.LoadingCache;

public final class MatrixCache {

	private IDatabase database;

	private FlowTypeTable flowTypeTable;
	private ConversionTable conversionTable;

	private LoadingCache<Long, List<CalcAllocationFactor>> allocationCache;
	private LoadingCache<Long, List<CalcImpactFactor>> impactCache;
	private LoadingCache<Long, List<CalcExchange>> exchangeCache;

	public static MatrixCache create(IDatabase database) {
		return new MatrixCache(database);
	}

	private MatrixCache(IDatabase database) {
		this.database = database;
		flowTypeTable = FlowTypeTable.create(database);
		conversionTable = ConversionTable.create(database);
		exchangeCache = ExchangeCache.create(database, conversionTable,
				flowTypeTable);
		allocationCache = AllocationCache.create(database);
		impactCache = ImpactFactorCache.create(database, conversionTable);
	}

	public LoadingCache<Long, List<CalcAllocationFactor>> getAllocationCache() {
		return allocationCache;
	}

	public LoadingCache<Long, List<CalcImpactFactor>> getImpactCache() {
		return impactCache;
	}

	public LoadingCache<Long, List<CalcExchange>> getExchangeCache() {
		return exchangeCache;
	}

}
