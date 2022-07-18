package org.openlca.core.io;

import java.util.Objects;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * An entity resolver that directly queries the database for each request.
 */
public class DbEntityResolver implements EntityResolver {

	private final IDatabase db;

	public DbEntityResolver(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static DbEntityResolver of(IDatabase db) {
		return new DbEntityResolver(db);
	}

	@Override
	public IDatabase db() {
		return db;
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

	@Override
	public Category getCategory(ModelType type, String path) {
		var dao = new CategoryDao(db);
		return dao.getForPath(type, path);
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		if (providerId == null || exchange == null)
			return;
		var d = new ProcessDao(db).getDescriptorForRefId(providerId);
		exchange.defaultProviderId = d != null ? d.id : 0;
	}
}
