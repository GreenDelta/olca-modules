package org.openlca.io.olca;

import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProcessDao srcDao;
	private ProcessDao destDao;
	private IDatabase dest;
	private Sequence seq;

	//  Required for translating the default provider links.
	private TLongLongHashMap srcDestIdMap = new TLongLongHashMap();

	ProcessImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProcessDao(source);
		this.destDao = new ProcessDao(dest);
		this.dest = dest;
		this.seq = seq;
	}

	public void run() {
		log.trace("import processes");
		try {
			for (ProcessDescriptor descriptor : srcDao.getDescriptors()) {
				long destId = seq.get(seq.PROCESS, descriptor.getRefId());
				if (destId != 0)
					srcDestIdMap.put(descriptor.getId(), destId);
				else
					createProcess(descriptor);
			}
			switchDefaultProviders();
		} catch (Exception e) {
			log.error("failed to import processes", e);
		}
	}

	private void createProcess(ProcessDescriptor descriptor) {
		Process srcProcess = srcDao.getForId(descriptor.getId());
		Process destProcess = srcProcess.clone();
		destProcess.setRefId(srcProcess.getRefId());
		switchCategory(srcProcess, destProcess);
		switchLocation(srcProcess, destProcess);
		switchExchangeRefs(destProcess);
		switchAllocationProducts(srcProcess, destProcess);
		destProcess.getCostEntries().clear(); // TODO: remove
		// TODO: switchCostCategories(srcProcess, destProcess);
		switchDocRefs(destProcess);
		destProcess = destDao.insert(destProcess);
		seq.put(seq.PROCESS, srcProcess.getRefId(), destProcess.getId());
		srcDestIdMap.put(srcProcess.getId(), destProcess.getId());
	}

	private void switchLocation(Process srcProcess, Process destProcess) {
		if (srcProcess.getLocation() == null)
			return;
		long locId = seq.get(seq.LOCATION, srcProcess.getLocation().getRefId());
		LocationDao dao = new LocationDao(dest);
		destProcess.setLocation(dao.getForId(locId));
	}

	private void switchCategory(Process srcProcess, Process destProcess) {
		if (srcProcess.getCategory() == null)
			return;
		long catId = seq.get(seq.CATEGORY, srcProcess.getCategory().getRefId());
		CategoryDao destCategoryDao = new CategoryDao(dest);
		destProcess.setCategory(destCategoryDao.getForId(catId));
	}

	private void switchExchangeRefs(Process destProcess) {
		FlowDao flowDao = new FlowDao(dest);
		BaseDao<Unit> unitDao = dest.createDao(Unit.class);
		List<Exchange> removals = new ArrayList<>();
		for (Exchange exchange : destProcess.getExchanges()) {
			if (!isValid(exchange)) {
				removals.add(exchange);
				continue;
			}
			Flow destFlow = switchExchangeFlow(flowDao, exchange);
			switchExchangeProperty(exchange, destFlow);
			switchExchangeUnit(unitDao, exchange);
		}
		if (!removals.isEmpty()) {
			log.warn("there where invalid exchanges in {} " +
					"that where removed during the import", destProcess);
			destProcess.getExchanges().removeAll(removals);
		}
	}

	private boolean isValid(Exchange exchange) {
		return exchange.getFlow() != null
				&& exchange.getFlowPropertyFactor() != null
				&& exchange.getFlowPropertyFactor().getFlowProperty() != null
				&& exchange.getUnit() != null;
	}

	private Flow switchExchangeFlow(FlowDao flowDao, Exchange exchange) {
		Flow srcFlow = exchange.getFlow();
		long flowId = seq.get(seq.FLOW, srcFlow.getRefId());
		Flow destFlow = flowDao.getForId(flowId);
		exchange.setFlow(destFlow);
		return destFlow;
	}

	private void switchExchangeProperty(Exchange exchange, Flow destFlow) {
		FlowProperty srcProp = exchange.getFlowPropertyFactor()
				.getFlowProperty();
		long propId = seq.get(seq.FLOW_PROPERTY, srcProp.getRefId());
		FlowPropertyFactor destFac = null;
		for (FlowPropertyFactor fac : destFlow.getFlowPropertyFactors()) {
			if (fac.getFlowProperty() == null)
				continue;
			if (propId == fac.getFlowProperty().getId()) {
				destFac = fac;
				break;
			}
		}
		exchange.setFlowPropertyFactor(destFac);
	}

	private void switchExchangeUnit(BaseDao<Unit> unitDao, Exchange exchange) {
		Unit srcUnit = exchange.getUnit();
		long unitId = seq.get(seq.UNIT, srcUnit.getRefId());
		exchange.setUnit(unitDao.getForId(unitId));
	}

	private void switchAllocationProducts(Process srcProcess, Process destProcess) {
		for (AllocationFactor factor : destProcess.getAllocationFactors()) {
			long srcProductId = factor.getProductId();
			String srcRefId = null;
			for (Exchange srcExchange : srcProcess.getExchanges()) {
				if (srcExchange.getFlow() == null)
					continue;
				if (srcExchange.getFlow().getId() == srcProductId) {
					srcRefId = srcExchange.getFlow().getRefId();
				}
			}
			long destProductId = seq.get(seq.FLOW, srcRefId);
			factor.setProductId(destProductId);
		}
	}

	private void switchDocRefs(Process destProcess) {
		if (destProcess.getDocumentation() == null)
			return;
		ProcessDocumentation doc = destProcess.getDocumentation();
		doc.setReviewer(translateActor(doc.getReviewer()));
		doc.setDataGenerator(translateActor(doc.getDataGenerator()));
		doc.setDataDocumentor(translateActor(doc.getDataDocumentor()));
		doc.setDataSetOwner(translateActor(doc.getDataSetOwner()));
		doc.setPublication(translateSource(doc.getPublication()));
		List<Source> translatedSources = new ArrayList<>();
		for (Source source : doc.getSources())
			translatedSources.add(translateSource(source));
		doc.getSources().clear();
		doc.getSources().addAll(translatedSources);
	}

	private Actor translateActor(Actor srcActor) {
		if (srcActor == null)
			return null;
		long id = seq.get(seq.ACTOR, srcActor.getRefId());
		ActorDao actorDao = new ActorDao(dest);
		return actorDao.getForId(id);
	}

	private Source translateSource(Source srcSource) {
		if (srcSource == null)
			return null;
		long id = seq.get(seq.SOURCE, srcSource.getRefId());
		SourceDao dao = new SourceDao(dest);
		return dao.getForId(id);
	}

	private void switchDefaultProviders() {


	}
}
