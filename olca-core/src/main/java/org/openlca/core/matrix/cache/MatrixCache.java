package org.openlca.core.matrix.cache;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.ModelType;

import com.google.common.cache.LoadingCache;

public final class MatrixCache {

	private final boolean lazy;
	private final IDatabase database;

	private FlowTable flowTypeTable;
	private ConversionTable conversionTable;
	private ProviderMap providerMap;

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
			flowTypeTable = FlowTable.create(database);
			conversionTable = ConversionTable.create(database);
			providerMap = ProviderMap.create(database);
			exchangeCache = ExchangeCache.create(database, conversionTable,
					flowTypeTable);
		}
	}

	public IDatabase getDatabase() {
		return database;
	}

	private FlowTable getFlowTypeTable() {
		if (flowTypeTable == null)
			flowTypeTable = FlowTable.create(database);
		return flowTypeTable;
	}

	private ConversionTable getConversionTable() {
		if (conversionTable == null)
			conversionTable = ConversionTable.create(database);
		return conversionTable;
	}

	public ProviderMap getProcessTable() {
		if (providerMap == null)
			providerMap = ProviderMap.create(database);
		return providerMap;
	}

	public LoadingCache<Long, List<CalcExchange>> getExchangeCache() {
		if (exchangeCache == null)
			exchangeCache = ExchangeCache.create(database,
					getConversionTable(), getFlowTypeTable());
		return exchangeCache;
	}

	public synchronized void evictAll() {
		if (flowTypeTable != null)
			flowTypeTable.reload(database);
		if (conversionTable != null)
			conversionTable.reload();
		if (exchangeCache != null)
			exchangeCache.invalidateAll();
		providerMap = null;
	}

	public synchronized void evict(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
			case FLOW, FLOW_PROPERTY, UNIT_GROUP:
				baseEviction();
				break;
			case PROCESS:
				evictProcess(id);
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
		} else {
			conversionTable.reload();
			flowTypeTable.reload(database);
			exchangeCache.invalidateAll();
		}
		providerMap = null;
	}

	private void evictProcess(long id) {
		providerMap = null;
		if (exchangeCache != null)
			exchangeCache.invalidate(id);
	}

	public synchronized void registerNew(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
			case FLOW, FLOW_PROPERTY, UNIT_GROUP:
				baseEviction();
				break;
			case PROCESS:
				providerMap = null;
				break;
			default:
				break;
		}
	}

}
