package org.openlca.core.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * An entity resolver that directly queries the database for each request.
 */
public record DbEntityResolver(IDatabase db) implements EntityResolver {

	public static DbEntityResolver of(IDatabase db) {
		return new DbEntityResolver(db);
	}

	@Override
	public <T extends RootEntity> T get(Class<T> type, String refId) {
		return db.get(type, refId);
	}

	@Override
	public <T extends RootEntity> Descriptor getDescriptor(
		Class<T> type, String refId) {
		return db.getDescriptor(type, refId);
	}
}
