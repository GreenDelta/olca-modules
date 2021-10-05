package org.openlca.io.olca;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ProcessDao srcDao;
	private final ProcessDao destDao;
	private final IDatabase dest;
	private final RefSwitcher refs;
	private final Sequence seq;

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
				long destId = seq.get(seq.PROCESS, descriptor.refId);
				if (destId != 0)
					srcDestIdMap.put(descriptor.id, destId);
				else
					createProcess(descriptor);
			}
			switchDefaultProviders();
		} catch (Exception e) {
			log.error("failed to import processes", e);
		}
	}

	private void createProcess(ProcessDescriptor descriptor) {
		Process srcProcess = srcDao.getForId(descriptor.id);
		Process destProcess = srcProcess.copy();
		destProcess.refId = srcProcess.refId;
		destProcess.category = refs.switchRef(srcProcess.category);
		destProcess.location = refs.switchRef(srcProcess.location);
		Set<Long> providerUpdates = switchExchangeRefs(destProcess);
		switchAllocationProducts(srcProcess, destProcess);
		switchDocRefs(destProcess);
		switchSocialAspectRefs(destProcess);
		switchDqSystems(destProcess);
		destProcess = destDao.insert(destProcess);
		seq.put(seq.PROCESS, srcProcess.refId, destProcess.id);
		srcDestIdMap.put(srcProcess.id, destProcess.id);
		putProviderUpdates(providerUpdates, destProcess);
	}

	private void putProviderUpdates(Set<Long> providerUpdates,
			Process destProcess) {
		for (Exchange exchange : destProcess.exchanges) {
			if (exchange.defaultProviderId >= 0)
				continue;
			// old default providers have a negative sign
			long oldId = Math.abs(exchange.defaultProviderId);
			oldProviderMap.put(exchange.id, oldId);
		}
	}

	/**
	 * Returns also the list of provider IDs from the source database that need
	 * to be updated after the import.
	 */
	private Set<Long> switchExchangeRefs(Process destProcess) {
		List<Exchange> removals = new ArrayList<>();
		Set<Long> oldProviders = new HashSet<>();
		for (Exchange e : destProcess.exchanges) {
			if (!isValid(e)) {
				removals.add(e);
				continue;
			}
			checkSetProvider(e, oldProviders);
			e.flow = refs.switchRef(e.flow);
			e.flowPropertyFactor = refs.switchRef(
					e.flowPropertyFactor, e.flow);
			e.unit = refs.switchRef(e.unit);
			e.currency = refs.switchRef(e.currency);
		}
		if (!removals.isEmpty()) {
			log.warn("there where invalid exchanges in {} "
					+ "that where removed during the import", destProcess);
			destProcess.exchanges.removeAll(removals);
		}
		return oldProviders;
	}

	private void switchSocialAspectRefs(Process destProcess) {
		for (SocialAspect aspect : destProcess.socialAspects) {
			aspect.indicator = refs.switchRef(aspect.indicator);
			aspect.source = refs.switchRef(aspect.source);
		}
	}

	private void checkSetProvider(Exchange exchange, Set<Long> oldProviders) {
		long oldId = exchange.defaultProviderId;
		if (oldId <= 0)
			return; // no default provider
		long newId = srcDestIdMap.get(oldId);
		if (newId != 0) {
			exchange.defaultProviderId = newId;
			return; // default provider already in database
		}
		// update required after import indicated by a negative sign
		exchange.defaultProviderId = -oldId;
		oldProviders.add(oldId);
	}

	private boolean isValid(Exchange exchange) {
		return exchange.flow != null
				&& exchange.flowPropertyFactor != null
				&& exchange.flowPropertyFactor.flowProperty != null
				&& exchange.unit != null;
	}

	private void switchAllocationProducts(Process srcProcess,
			Process destProcess) {
		for (AllocationFactor factor : destProcess.allocationFactors) {
			long srcProductId = factor.productId;
			String srcRefId = null;
			for (Exchange srcExchange : srcProcess.exchanges) {
				if (srcExchange.flow == null)
					continue;
				if (srcExchange.flow.id == srcProductId) {
					srcRefId = srcExchange.flow.refId;
				}
			}
			factor.productId = seq.get(seq.FLOW, srcRefId);
		}
	}

	private void switchDocRefs(Process destProcess) {
		if (destProcess.documentation == null)
			return;
		ProcessDocumentation doc = destProcess.documentation;
		doc.reviewer = refs.switchRef(doc.reviewer);
		doc.dataGenerator = refs.switchRef(doc.dataGenerator);
		doc.dataDocumentor = refs.switchRef(doc.dataDocumentor);
		doc.dataSetOwner = refs.switchRef(doc.dataSetOwner);
		doc.publication = refs.switchRef(doc.publication);
		List<Source> translatedSources = new ArrayList<>();
		for (Source source : doc.sources)
			translatedSources.add(refs.switchRef(source));
		doc.sources.clear();
		doc.sources.addAll(translatedSources);
	}

	private void switchDqSystems(Process destProcess) {
		destProcess.dqSystem = refs.switchRef(destProcess.dqSystem);
		destProcess.exchangeDqSystem = refs
				.switchRef(destProcess.exchangeDqSystem);
		destProcess.socialDqSystem = refs.switchRef(destProcess.socialDqSystem);
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
					(int i, PreparedStatement ps) -> {
						ps.setLong(1, providerIds.get(i));
						ps.setLong(2, exchangeIds.get(i));
						return true;
					});
		} catch (Exception e) {
			log.error("failed to update default provider", e);
		}
	}
}
