package org.openlca.io.olca;

import java.util.ArrayList;
import java.util.Objects;

import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProviderType;

/// Copies and imports the processes into the target database. While copying the
/// processes, the default providers could be not present in the target database
/// yet. In this case, we set the default provider ID of an exchange to the
/// negative value of the corresponding ID in the source database. After the
/// import of all possible provider types (also results and product systems),
/// we then search for negative provider IDs and replace them.
final class ProcessTransfer implements EntityTransfer<Process> {

	private final TransferConfig conf;
	private final ImportLog log;

	ProcessTransfer(TransferConfig config) {
		this.conf = config;
		this.log = config.log();
	}

	@Override
	public void syncAll() {
		for (var d : conf.source().getDescriptors(Process.class)) {
			var origin = conf.source().get(Process.class, d.id);
			sync(origin);
		}
	}

	@Override
	public Process sync(Process origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();

			copy.location = conf.swap(origin.location);
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
			return copy;
		});
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
			e.flowPropertyFactor = conf.mapFactor(e.flow, e.flowPropertyFactor);
			e.unit = conf.mapUnit(e.flowPropertyFactor, e.unit);
			e.currency = conf.swap(e.currency);
			e.location = conf.swap(e.location);
			mapDefaultProvider(e);
		}

		if (!removals.isEmpty()) {
			log.warn(copy,
				"had invalid exchanges that were removed in the import");
			copy.exchanges.removeAll(removals);
		}
	}

	private void mapDefaultProvider(Exchange e) {
		if (e.defaultProviderId == 0)
			return;
		var type = ProviderType.toModelType(e.defaultProviderType);
		var destId = conf.seq().get(type, e.defaultProviderId);
		if (destId != 0) {
			e.defaultProviderId = destId;
			return;
		}
		// set it to the negative value of the original ID to be replaced later
		e.defaultProviderId = -e.defaultProviderId;
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
}
