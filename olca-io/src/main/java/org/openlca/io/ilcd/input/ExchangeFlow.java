package org.openlca.io.ilcd.input;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeFlow {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Exchange ilcdExchange;
	private IDatabase database;
	private DataStore dataStore;
	private FlowMap flowMap;

	private Flow flow;
	private FlowMapEntry mapEntry;

	public ExchangeFlow(Exchange ilcdExchange) {
		this.ilcdExchange = ilcdExchange;
	}

	public Flow getFlow() {
		return flow;
	}

	public FlowMapEntry getMapEntry() {
		return mapEntry;
	}

	public boolean isMapped() {
		return mapEntry != null;
	}

	public void findOrImport(IDatabase database, DataStore dataStore,
			FlowMap flowMap) {
		this.database = database;
		this.dataStore = dataStore;
		this.flowMap = flowMap;
		DataSetReference ref = ilcdExchange.getFlow();
		if (ref == null) {
			log.warn("ILCD exchange without flow ID: {}", ilcdExchange);
			return;
		}
		try {
			this.flow = fetch(ref.getUuid());
		} catch (Exception e) {
			log.error("failed to get flow ", e);
		}
	}

	private Flow fetch(String uuid) {
		Flow flow = fetchFromCache(uuid);
		if (flow != null)
			return flow;
		flow = fetchFromDatabase(uuid);
		if (flow != null)
			return cache(uuid, flow);
		flow = fetchFromFlowMap(uuid);
		if (flow != null)
			return cache(uuid, flow);
		flow = fetchFromImport(uuid);
		return cache(uuid, flow);
	}

	private Flow cache(String id, Flow flow) {
		if (flowMap != null)
			flowMap.cache(id, flow);
		return flow;
	}

	private Flow fetchFromCache(String uuid) {
		if (flowMap == null)
			return null;
		else
			return flowMap.getCached(uuid);
	}

	private Flow fetchFromDatabase(String flowId) {
		try {
			FlowDao dao = new FlowDao(database);
			return dao.getForRefId(flowId);
		} catch (Exception e) {
			log.error("Cannot get flow", e);
			return null;
		}
	}

	private Flow fetchFromFlowMap(String flowId) {
		if (flowMap == null)
			return null;
		mapEntry = flowMap.getEntry(flowId);
		if (mapEntry != null)
			return fetchFromDatabase(mapEntry.getOpenlcaFlowKey());
		else
			return null;
	}

	private Flow fetchFromImport(String flowId) {
		try {
			FlowImport flowImport = new FlowImport(dataStore, database);
			return flowImport.run(flowId);
		} catch (Exception e) {
			log.error("Cannot get flow", e);
			return null;
		}
	}

}
