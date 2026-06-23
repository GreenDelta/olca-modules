package org.openlca.io.olca.migration;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.RootDescriptor;

/// Configuration for migrating entities from a source database to a
/// target database. Supported root entities: projects, product systems,
/// and impact methods.
///
/// Besides the source and target databases and the entities to migrate,
/// this configuration contains an ordered array of provider-matching
/// strategies. When a product input or waste output needs to be linked
/// to a provider, the strategies are applied in order until a matching
/// provider is found in the target database. If no such provider is
/// found, the provider from the source database is copied to the target
/// database. If `allProcesses` is true, all processes from the source
/// are migrated regardless of the entity list.
public record MigrationConfig(
	IDatabase source,
	IDatabase target,
	List<RootDescriptor> entities,
	boolean allProcesses,
	MatchingStrategy[] strategies) {

	boolean isNotComplete() {
		if(source == null
			|| target == null
			|| strategies == null
			|| strategies.length == 0)
			return true;
		return (entities == null || entities.isEmpty()) && !allProcesses;
	}
}
