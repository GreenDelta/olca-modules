package org.openlca.io.ilcd.input;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeFlow {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private Exchange ilcdExchange;

	Process process;
	Flow flow;
	FlowMapEntry mapEntry;
	FlowProperty flowProperty;
	Unit unit;

	public ExchangeFlow(Exchange ilcdExchange) {
		this.ilcdExchange = ilcdExchange;
	}

	public boolean isMapped() {
		return mapEntry != null;
	}

	public void findOrImport(ImportConfig config) {
		this.config = config;
		Ref ref = ilcdExchange.flow;
		if (ref == null) {
			log.warn("ILCD exchange without flow ID: {}", ilcdExchange);
			return;
		}
		try {
			this.flow = fetch(ref.uuid);
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
			return flow; // do not cache mapped flows! -> TODO: but we should!
		flow = fetchFromImport(uuid);
		return cache(uuid, flow);
	}

	private Flow cache(String id, Flow flow) {
		FlowMap flowMap = config.getFlowMap();
		flowMap.cache(id, flow);
		return flow;
	}

	private Flow fetchFromCache(String uuid) {
		FlowMap flowMap = config.getFlowMap();
		return flowMap.getCached(uuid);
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
		FlowMap flowMap = config.getFlowMap();
		FlowMapEntry e = flowMap.getEntry(flowId);
		if (e == null)
			return null;
		String mappedID = e.referenceFlowID;
		Flow f = fetchFromCache(mappedID);
		if (f == null) {
			f = fetchFromDatabase(mappedID);
		}
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

	boolean isValid() {
		if (flow == null)
			return false;
		FlowProperty property = flowProperty;
		if (property == null) {
			property = flow.getReferenceFlowProperty();
		}
		if (property == null || flow.getFactor(property) == null)
			return false;
		UnitGroup group = property.getUnitGroup();
		if (group == null)
			return false;
		if ((unit == null || group.getUnit(unit.getName()) == null) && group.getReferenceUnit() == null)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Exchange [flow=" + flow + ", flowProperty=" + flowProperty + ", unit=" + unit + "]";
	}

}
