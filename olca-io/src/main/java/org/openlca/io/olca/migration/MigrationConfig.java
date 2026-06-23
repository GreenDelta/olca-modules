package org.openlca.io.olca.migration;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.RootDescriptor;

/// Configuration for transferring a product system from a source database to a
/// target database.
///
/// Besides the source and target databases and the product system to transfer,
/// this configuration contains an ordered array of provider-matching
/// strategies. When a product input or waste output of the product system needs
/// to be linked to a provider, the strategies are applied in order until a
/// matching provider is found in the target database. If no such provider is
/// found, the provider from the source database is copied to the target
/// database.
public record MigrationConfig(
	IDatabase source,
	IDatabase target,
	List<RootDescriptor> entities,
	boolean allProcesses,
	MatchingStrategy[] strategies) {

	boolean isNotComplete() {
		return source == null
			|| target == null
			|| entities == null
			|| entities.isEmpty()
			|| strategies == null
			|| strategies.length == 0;
	}
}
