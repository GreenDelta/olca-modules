package org.openlca.core.matrix.cache;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcCostEntry;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;

import com.google.common.cache.LoadingCache;

public final class MatrixCache {

	private IDatabase database;
	private FlowTypeTable flowTypeTable;
	private ConversionTable conversionTable;
	private ProcessTable processTable;

	private LoadingCache<Long, List<CalcAllocationFactor>> allocationCache;
	private LoadingCache<Long, List<CalcImpactFactor>> impactCache;
	private LoadingCache<Long, List<CalcExchange>> exchangeCache;
	private LoadingCache<Long, List<CalcCostEntry>> costCache;

	public static MatrixCache create(IDatabase database) {
		return new MatrixCache(database);
	}

	private MatrixCache(IDatabase database) {
		this.database = database;
		flowTypeTable = FlowTypeTable.create(database);
		conversionTable = ConversionTable.create(database);
		processTable = ProcessTable.create(database);
		exchangeCache = ExchangeCache.create(database, conversionTable,
				flowTypeTable);
		allocationCache = AllocationCache.create(database);
		impactCache = ImpactFactorCache.create(database, conversionTable);
		costCache = CostEntryCache.create(database);
	}

	public IDatabase getDatabase() {
		return database;
	}

	public ProcessTable getProcessTable() {
		return processTable;
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

	public LoadingCache<Long, List<CalcCostEntry>> getCostCache() {
		return costCache;
	}

}
