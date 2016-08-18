package org.openlca.io.olca;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProcessDao srcDao;
	private ProcessDao destDao;
	private IDatabase dest;
	private RefSwitcher refs;
	private Sequence seq;

	// Required for translating the default provider links: we import exchanges
	// with possible links to processes that are not yet imported
	private TLongLongHashMap srcDestIdMap = new TLongLongHashMap();
	// Contains the exchange IDs and old default provider IDs that need to be
	// updated after the import.
	private TLongLongHashMap oldProviderMap = new TLongLongHashMap();

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
		Set<Long> providerUpdates = switchExchangeRefs(destProcess);
		switchAllocationProducts(srcProcess, destProcess);
		switchDocRefs(destProcess);
		destProcess = destDao.insert(destProcess);
		seq.put(seq.PROCESS, srcProcess.getRefId(), destProcess.getId());
		srcDestIdMap.put(srcProcess.getId(), destProcess.getId());
		putProviderUpdates(providerUpdates, destProcess);
	}

	private void putProviderUpdates(Set<Long> providerUpdates,
			Process destProcess) {
		for (Exchange exchange : destProcess.getExchanges()) {
			if (exchange.getDefaultProviderId() >= 0)
				continue;
			// old default providers have a negative sign
			long oldId = Math.abs(exchange.getDefaultProviderId());
			oldProviderMap.put(exchange.getId(), oldId);
		}
	}

	/**
	 * Returns also the list of provider IDs from the source database that need
	 * to be updated after the import.
	 */
	private Set<Long> switchExchangeRefs(Process destProcess) {
		List<Exchange> removals = new ArrayList<>();
		Set<Long> oldProviders = new HashSet<>();
		for (Exchange e : destProcess.getExchanges()) {
			if (!isValid(e)) {
				removals.add(e);
				continue;
			}
			checkSetProvider(e, oldProviders);
			Flow destFlow = refs.switchRef(e.getFlow());
			e.setFlow(destFlow);
			e.setFlowPropertyFactor(refs.switchRef(
					e.getFlowPropertyFactor(), destFlow));
			e.setUnit(refs.switchRef(e.getUnit()));
			e.currency = refs.switchRef(e.currency);
		}
		if (!removals.isEmpty()) {
			log.warn("there where invalid exchanges in {} "
					+ "that where removed during the import", destProcess);
			destProcess.getExchanges().removeAll(removals);
		}
		return oldProviders;
	}

	private void checkSetProvider(Exchange exchange, Set<Long> oldProviders) {
		long oldId = exchange.getDefaultProviderId();
		if (oldId <= 0)
			return; // no default provider
		long newId = srcDestIdMap.get(oldId);
		if (newId != 0) {
			exchange.setDefaultProviderId(newId);
			return; // default provider already in database
		}
		// update required after import indicated by a negative sign
		exchange.setDefaultProviderId(-oldId);
		oldProviders.add(oldId);
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
		TLongArrayList exchangeIds = new TLongArrayList();
		TLongArrayList providerIds = new TLongArrayList();
		TLongLongIterator it = oldProviderMap.iterator();
		while (it.hasNext()) {
			it.advance();
			long exchangeId = it.key();
			long newId = srcDestIdMap.get(it.value());
			exchangeIds.add(exchangeId);
			providerIds.add(newId);
		}
		updateDefaultProviders(exchangeIds, providerIds);
	}

	private void updateDefaultProviders(final TLongArrayList exchangeIds,
			final TLongArrayList providerIds) {
		String stmt = "update tbl_exchanges set f_default_provider = ? where id = ?";
		try {
			NativeSql.on(dest).batchInsert(stmt, exchangeIds.size(),
					new NativeSql.BatchInsertHandler() {
						@Override
						public boolean addBatch(int i, PreparedStatement stmt)
								throws SQLException {
							stmt.setLong(1, providerIds.get(i));
							stmt.setLong(2, exchangeIds.get(i));
							return true;
						}
					});
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}
}
