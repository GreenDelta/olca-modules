package org.openlca.io.ilcd.input;

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
		if (ref != null) {
			run(ref.getUuid());
		}
	}

	private void run(String uuid) {
		fetchFromDatabase(uuid);
		if (flow == null && flowMap != null) {
			fetchFromFlowMap(uuid);
		}
		if (flow == null) {
			fetchFromImport(uuid);
		}
	}

	private void fetchFromDatabase(String flowId) {
		try {
			flow = database.select(Flow.class, flowId);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow", e);
		}
	}

	private void fetchFromFlowMap(String flowId) {
		mapEntry = flowMap.getEntry(flowId);
		if (mapEntry != null) {
			fetchFromDatabase(mapEntry.getOpenlcaFlowKey());
		}
	}

	private void fetchFromImport(String flowId) {
		try {
			FlowImport flowImport = new FlowImport(dataStore, database);
			flowImport.run(flowId);
			fetchFromDatabase(flowId);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow", e);
		}
	}

}
