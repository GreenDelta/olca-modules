package org.openlca.io.ilcd.input;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeFlow {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private final Exchange iExchange;

	Flow flow;
	FlowMapEntry mapEntry;
	FlowProperty flowProperty;
	Unit unit;

	ExchangeFlow(Exchange iExchange) {
		this.iExchange = iExchange;
	}

	boolean isMapped() {
		return mapEntry != null;
	}

	/**
	 * Returns a possible provider for the flow when the flow is a mapped flow
	 * and there is a provider specified for that flow.
	 */
	ProcessDescriptor getMappedProvider() {
		if (flow == null
			|| flow.flowType == FlowType.ELEMENTARY_FLOW)
			return null;
		if (mapEntry == null)
			return null;
		if (mapEntry.targetFlow() == null)
			return null;
		return mapEntry.targetFlow().provider;
	}

	void findOrImport(ImportConfig config) {
		this.config = config;
		Ref ref = iExchange.flow;
		if (ref == null) {
			log.warn("ILCD exchange without flow ID: {}", iExchange);
			return;
		}
		try {
			this.flow = fetch(ref.uuid);
		} catch (Exception e) {
			log.error("failed to get flow ", e);
		}
	}

	private Flow fetch(String uuid) {
		Flow flow = config.flowCache().get(uuid);
		if (flow != null)
			return flow;
		flow = fetchFromFlowMap(uuid);
		if (flow != null) {
			// the flow is not cached here because
			// the UUID of the mapped flow may is
			// different and the mapping entry
			// needs to be initialized, see below.
			return flow;
		}
		flow = fetchFromDatabase(uuid);
		if (flow != null) {
			config.flowCache().put(uuid, flow);
			return flow;
		}
		flow = FlowImport.get(config, uuid);
		config.flowCache().put(uuid, flow);
		return flow;
	}

	private Flow fetchFromDatabase(String flowId) {
		try {
			FlowDao dao = new FlowDao(config.db());
			return dao.getForRefId(flowId);
		} catch (Exception e) {
			log.error("Cannot get flow", e);
			return null;
		}
	}

	private Flow fetchFromFlowMap(String flowId) {
		FlowMap flowMap = config.flowMap();
		FlowMapEntry e = flowMap.getEntry(flowId);
		if (e == null)
			return null;
		String targetID = e.targetFlowId();
		Flow f = config.flowCache().get(targetID);
		if (f == null) {
			f = fetchFromDatabase(targetID);
			config.flowCache().put(targetID, f);
		}
		if (f != null) {
			mapEntry = e;
		}
		return f;
	}

	boolean isValid() {
		if (flow == null)
			return false;
		FlowProperty property = flowProperty;
		if (property == null) {
			property = flow.referenceFlowProperty;
		}
		if (property == null || flow.getFactor(property) == null)
			return false;
		UnitGroup group = property.unitGroup;
		if (group == null)
			return false;
		return (unit != null && group.getUnit(unit.name) != null)
			|| group.referenceUnit != null;
	}

	@Override
	public String toString() {
		return "Exchange [flow=" + flow
			+ ", flowProperty=" + flowProperty
			+ ", unit=" + unit + "]";
	}

}
