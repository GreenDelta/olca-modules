package org.openlca.core.io;

import java.util.Objects;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Strings;

/**
 * An entity resolver that directly queries the database for each request.
 */
public class DbEntityResolver implements EntityResolver {

	private final IDatabase db;
	private boolean createCategories = false;

	private DbEntityResolver(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static DbEntityResolver of(IDatabase db) {
		return new DbEntityResolver(db);
	}

	/**
	 * If set to true, this resolver will create requested categories that do
	 * not exist yet.
	 */
	public DbEntityResolver withCategoryCreation(boolean b) {
		createCategories = b;
		return this;
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
		if (type == null || Strings.nullOrEmpty(path))
			return null;
		var dao = new CategoryDao(db);
		var c = dao.getForPath(type, path);
		return c == null && createCategories
				? dao.sync(type, path.split("/"))
				: c;
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		if (providerId == null || exchange == null)
			return;
		var type = ProviderType.toModelClass(exchange.defaultProviderType);
		var d = db.getDescriptor(type, providerId);
		exchange.defaultProviderId = d != null ? d.id : 0;
	}
}
