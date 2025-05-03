package org.openlca.io.olca;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Objects;

class ProcessImport {

	private final Config conf;
	private final ImportLog log;
	private final ProcessDao srcDao;
	private final ProcessDao destDao;
	private final RefSwitcher refs;

	// Required for translating the default provider links: we import exchanges
	// with possible links to processes that are not yet imported
	private final TLongLongHashMap providerMap = new TLongLongHashMap();

	// Contains the exchange IDs and old default provider IDs that need to be
	// updated after the import.
	private final TLongLongHashMap oldExchangeProviders = new TLongLongHashMap();

	private ProcessImport(Config config) {
		this.conf = config;
		this.log = config.log();
		this.srcDao = new ProcessDao(config.source());
		this.destDao = new ProcessDao(config.target());
		this.refs = new RefSwitcher(conf);
	}

	static void run(Config conf) {
		new ProcessImport(conf).run();
	}

	private void run() {
		for (var d : srcDao.getDescriptors()) {
			try {
				long destId = conf.seq().get(ModelType.PROCESS, d.id);
				if (destId != 0) {
					providerMap.put(d.id, destId);
				} else {
					copy(d);
				}
			} catch (Exception e) {
				log.error("failed to copy process " + d.refId, e);
			}
		}
		swapDefaultProviders();
	}

	private void copy(ProcessDescriptor d) {

		// init copy
		var src = srcDao.getForId(d.id);
		if (src == null)
			return;
		var copy = src.copy();
		copy.refId = src.refId;

		// swap references
		copy.category = conf.swap(src.category);
		copy.location = conf.swap(src.location);
		copy.dqSystem = conf.swap(copy.dqSystem);
		copy.exchangeDqSystem = conf.swap(copy.exchangeDqSystem);
		copy.socialDqSystem = conf.swap(copy.socialDqSystem);

		swapExchangeRefs(copy);
		swapAllocationProducts(copy);
		swapDocRefs(copy);
		for (var a : copy.socialAspects) {
			a.indicator = conf.swap(a.indicator);
			a.source = conf.swap(a.source);
		}

		copy = destDao.insert(copy);
		conf.seq().put(ModelType.PROCESS, src.id, copy.id);
		providerMap.put(src.id, copy.id);

		// collect old default providers; they have a negative sign
		for (var e : copy.exchanges) {
			if (e.defaultProviderId >= 0)
				continue;
			oldExchangeProviders.put(e.id, Math.abs(e.defaultProviderId));
		}
	}

	/**
	 * Returns also the list of provider IDs from the source database that need
	 * to be updated after the import.
	 */
	private void swapExchangeRefs(Process copy) {
		var removals = new ArrayList<Exchange>();
		for (Exchange e : copy.exchanges) {
			if (!isValid(e)) {
				removals.add(e);
				continue;
			}

			// swap references
			e.flow = conf.swap(e.flow);
			e.flowPropertyFactor = refs.switchRef(e.flowPropertyFactor, e.flow);
			e.unit = e.unit != null
					? Config.findUnit(e.flowPropertyFactor, e.unit.refId)
					: null;
			e.currency = conf.swap(e.currency);
			e.location = conf.swap(e.location);

			// handle the default provider
			if (e.defaultProviderId > 0) {
				long oldId = e.defaultProviderId;
				long newId = providerMap.get(oldId);
				if (newId != 0) {
					// default provider already in database
					e.defaultProviderId = newId;
				} else {
					// update required after import indicated by a negative sign
					// we can handle it when IDs are available after insertion
					e.defaultProviderId = -oldId;
				}
			}
		}

		if (!removals.isEmpty()) {
			log.warn(copy,
					"had invalid exchanges that were removed in the import");
			copy.exchanges.removeAll(removals);
		}
	}


	private boolean isValid(Exchange e) {
		return e.flow != null
				&& e.flowPropertyFactor != null
				&& e.flowPropertyFactor.flowProperty != null
				&& e.unit != null;
	}

	private void swapAllocationProducts(Process copy) {
		for (var f : copy.allocationFactors) {
			if (f.productId != 0) {
				f.productId = conf.seq().get(ModelType.FLOW, f.productId);
			}
		}
	}

	private void swapDocRefs(Process copy) {
		if (copy.documentation == null)
			return;
		var doc = copy.documentation;
		doc.dataGenerator = conf.swap(doc.dataGenerator);
		doc.dataDocumentor = conf.swap(doc.dataDocumentor);
		doc.dataOwner = conf.swap(doc.dataOwner);
		doc.publication = conf.swap(doc.publication);

		// sources
		var sources = doc.sources.stream()
				.map(conf::swap)
				.filter(Objects::nonNull)
				.toList();
		doc.sources.clear();
		doc.sources.addAll(sources);

		// reviews
		for (var rev : doc.reviews) {
			rev.report = conf.swap(rev.report);
			var reviewers = rev.reviewers.stream()
					.map(conf::swap)
					.filter(Objects::nonNull)
					.toList();
			rev.reviewers.clear();
			rev.reviewers.addAll(reviewers);
		}

		// compliance declarations
		for (var dec : doc.complianceDeclarations) {
			dec.system = conf.swap(dec.system);
		}
	}

	private void swapDefaultProviders() {
		conf.target()
				.getEntityFactory()
				.getCache()
				.evictAll();

		var exchangeIds = new TLongArrayList();
		var providerIds = new TLongArrayList();
		var it = oldExchangeProviders.iterator();
		while (it.hasNext()) {
			it.advance();
			long exchangeId = it.key();
			long newId = providerMap.get(it.value());
			exchangeIds.add(exchangeId);
			providerIds.add(newId);
		}

		var stmt = "update tbl_exchanges set f_default_provider = ? where id = ?";
		try {
			NativeSql.on(conf.target()).batchInsert(stmt, exchangeIds.size(),
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
