package org.openlca.io.ilcd.input;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeFlow {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private Exchange ilcdExchange;

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

	public void findOrImport(ImportConfig config) {
		this.config = config;
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
			return flow; // do not cache mapped flows!
		flow = fetchFromImport(uuid);
		return cache(uuid, flow);
	}

	private Flow cache(String id, Flow flow) {
		if (config.flowMap != null)
			config.flowMap.cache(id, flow);
		return flow;
	}

	private Flow fetchFromCache(String uuid) {
		if (config.flowMap != null)
			return config.flowMap.getCached(uuid);
		return null;
	}

	private Flow fetchFromDatabase(String flowId) {
		try {
			FlowDao dao = new FlowDao(config.db);
			return dao.getForRefId(flowId);
		} catch (Exception e) {
			log.error("Cannot get flow", e);
			return null;
		}
	}

	private Flow fetchFromFlowMap(String flowId) {
		if (config.flowMap == null)
			return null;
		FlowMapEntry e = config.flowMap.getEntry(flowId);
		if (e == null)
			return null;
		Flow f = fetch(e.getOpenlcaFlowKey());
		if (f != null) {
			mapEntry = e;
		}
		return f;
	}

	private Flow fetchFromImport(String flowId) {
		try {
			FlowImport flowImport = new FlowImport(config);
			return flowImport.run(flowId);
		} catch (Exception e) {
			log.error("Cannot get flow", e);
			return null;
		}
	}

}
