package org.openlca.io.olca;

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
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
	private RefSwitcher refs;
	private Sequence seq;

	// Required for translating the default provider links.
	private TLongLongHashMap srcDestIdMap = new TLongLongHashMap();

	ProcessImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProcessDao(source);
		this.destDao = new ProcessDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
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
		destProcess.setCategory(refs.switchRef(srcProcess.getCategory()));
		destProcess.setLocation(refs.switchRef(srcProcess.getLocation()));
		switchExchangeRefs(destProcess);
		switchAllocationProducts(srcProcess, destProcess);
		destProcess.getCostEntries().clear(); // TODO: remove
		// TODO: switchCostCategories(srcProcess, destProcess);
		switchDocRefs(destProcess);
		destProcess = destDao.insert(destProcess);
		seq.put(seq.PROCESS, srcProcess.getRefId(), destProcess.getId());
		srcDestIdMap.put(srcProcess.getId(), destProcess.getId());
	}

	private void switchExchangeRefs(Process destProcess) {
		List<Exchange> removals = new ArrayList<>();
		for (Exchange exchange : destProcess.getExchanges()) {
			if (!isValid(exchange)) {
				removals.add(exchange);
				continue;
			}
			Flow destFlow = refs.switchRef(exchange.getFlow());
			exchange.setFlow(destFlow);
			exchange.setFlowPropertyFactor(refs.switchRef(
					exchange.getFlowPropertyFactor(), destFlow));
			exchange.setUnit(refs.switchRef(exchange.getUnit()));
		}
		if (!removals.isEmpty()) {
			log.warn("there where invalid exchanges in {} "
					+ "that where removed during the import", destProcess);
			destProcess.getExchanges().removeAll(removals);
		}
	}

	private boolean isValid(Exchange exchange) {
		return exchange.getFlow() != null
				&& exchange.getFlowPropertyFactor() != null
				&& exchange.getFlowPropertyFactor().getFlowProperty() != null
				&& exchange.getUnit() != null;
	}

	private void switchAllocationProducts(Process srcProcess,
			Process destProcess) {
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
		doc.setReviewer(refs.switchRef(doc.getReviewer()));
		doc.setDataGenerator(refs.switchRef(doc.getDataGenerator()));
		doc.setDataDocumentor(refs.switchRef(doc.getDataDocumentor()));
		doc.setDataSetOwner(refs.switchRef(doc.getDataSetOwner()));
		doc.setPublication(refs.switchRef(doc.getPublication()));
		List<Source> translatedSources = new ArrayList<>();
		for (Source source : doc.getSources())
			translatedSources.add(refs.switchRef(source));
		doc.getSources().clear();
		doc.getSources().addAll(translatedSources);
	}

	private void switchDefaultProviders() {
		log.trace("update default providers");
		dest.getEntityFactory().getCache().evictAll();
		TLongLongIterator it = srcDestIdMap.iterator();
		while (it.hasNext()) {
			it.advance();
			long sourceId = it.key();
			long destId = it.value();
			if (sourceId == destId)
				continue;
			updateDefaultProvider(sourceId, destId);
		}
	}

	private void updateDefaultProvider(long sourceId, long destId) {
		try {
			String stmt = "update tbl_exchanges set f_default_provider = "
					+ destId + " where f_default_provider = " + sourceId;
			NativeSql.on(dest).runUpdate(stmt);
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}
}
