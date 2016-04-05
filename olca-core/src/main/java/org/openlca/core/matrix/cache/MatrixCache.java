package org.openlca.core.matrix.cache;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.model.ModelType;

import com.google.common.cache.LoadingCache;

public final class MatrixCache {

	private final boolean lazy;
	private final IDatabase database;

	private FlowTypeTable flowTypeTable;
	private ConversionTable conversionTable;
	private ProcessTable processTable;

	private LoadingCache<Long, List<CalcAllocationFactor>> allocationCache;
	private LoadingCache<Long, List<CalcImpactFactor>> impactCache;
	private LoadingCache<Long, List<CalcExchange>> exchangeCache;

	public static MatrixCache createEager(IDatabase database) {
		return new MatrixCache(database, false);
	}

	public static MatrixCache createLazy(IDatabase database) {
		return new MatrixCache(database, true);
	}

	private MatrixCache(IDatabase database, boolean lazy) {
		this.database = database;
		this.lazy = lazy;
		if (!lazy) {
			flowTypeTable = FlowTypeTable.create(database);
			conversionTable = ConversionTable.create(database);
			processTable = ProcessTable.create(database);
			exchangeCache = ExchangeCache.create(database, conversionTable,
					flowTypeTable);
			allocationCache = AllocationCache.create(database);
			impactCache = ImpactFactorCache.create(database, conversionTable);
		}
	}

	public IDatabase getDatabase() {
		return database;
	}

	private FlowTypeTable getFlowTypeTable() {
		if (flowTypeTable == null)
			flowTypeTable = FlowTypeTable.create(database);
		return flowTypeTable;
	}

	private ConversionTable getConversionTable() {
		if (conversionTable == null)
			conversionTable = ConversionTable.create(database);
		return conversionTable;
	}

	public ProcessTable getProcessTable() {
		if (processTable == null)
			processTable = ProcessTable.create(database);
		return processTable;
	}

	public LoadingCache<Long, List<CalcAllocationFactor>> getAllocationCache() {
		if (allocationCache == null)
			allocationCache = AllocationCache.create(database);
		return allocationCache;
	}

	public LoadingCache<Long, List<CalcImpactFactor>> getImpactCache() {
		if (impactCache == null)
			impactCache = ImpactFactorCache.create(database,
					getConversionTable());
		return impactCache;
	}

	public LoadingCache<Long, List<CalcExchange>> getExchangeCache() {
		if (exchangeCache == null)
			exchangeCache = ExchangeCache.create(database,
					getConversionTable(), getFlowTypeTable());
		return exchangeCache;
	}

	public synchronized void evictAll() {
		if (flowTypeTable != null)
			flowTypeTable.reload();
		if (conversionTable != null)
			conversionTable.reload();
		if (processTable != null)
			processTable.reload();
		if (exchangeCache != null)
			exchangeCache.invalidateAll();
		if (allocationCache != null)
			allocationCache.invalidateAll();
		if (impactCache != null)
			impactCache.invalidateAll();
	}

	public synchronized void evict(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
		case FLOW:
			baseEviction();
			break;
		case FLOW_PROPERTY:
			baseEviction();
			break;
		case IMPACT_CATEGORY:
			if (impactCache != null)
				impactCache.invalidate(id);
			break;
		case IMPACT_METHOD:
			if (impactCache != null)
				impactCache.invalidateAll();
			break;
		case PROCESS:
			evictProcess(id);
			break;
		case UNIT:
			baseEviction();
			break;
		case UNIT_GROUP:
			baseEviction();
			break;
		default:
			break;
		}
	}

	private void baseEviction() {
		if (conversionTable == null && flowTypeTable == null)
			return; // there cannot be an exchange or impact cache
		if (lazy) {
			conversionTable = null;
			flowTypeTable = null;
			exchangeCache = null;
			impactCache = null;
		} else {
			conversionTable.reload();
			flowTypeTable.reload();
			exchangeCache.invalidateAll();
			impactCache.invalidateAll();
		}
	}

	private void evictProcess(long id) {
		reloadProcessTable();
		if (exchangeCache != null)
			exchangeCache.invalidate(id);
		if (allocationCache != null)
			allocationCache.invalidate(id);
	}

	private void reloadProcessTable() {
		if (lazy)
			processTable = null;
		else
			processTable.reload();
	}

	public synchronized void registerNew(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
		case FLOW:
			baseEviction();
			break;
		case FLOW_PROPERTY:
			baseEviction();
			break;
		case PROCESS:
			reloadProcessTable();
			break;
		case UNIT:
			baseEviction();
			break;
		case UNIT_GROUP:
			baseEviction();
			break;
		default:
			break;
		}
	}

}
