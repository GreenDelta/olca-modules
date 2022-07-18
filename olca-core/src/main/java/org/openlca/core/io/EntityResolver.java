package org.openlca.core.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * This interface abstracts away the loading of entities from some data store.
 * Implementations could be for example databases, caches, imports, or
 * combinations of that.
 */
public interface EntityResolver {

	/**
	 * Optionally returns the database of the resolver. Note that this method
	 * returns {@code null} if there is no database attached to this resilver.
	 */
	default IDatabase db() {
		return null;
	}

	/**
	 * Tries to resolve an entity of the given type and with the given reference
	 * ID.
	 *
	 * @param type  the type of the requested entity
	 * @param refId the reference ID of that entity
	 * @param <T>   the type of the requested entity
	 * @return the requested entity if it could be resolved or {@code null}
	 * otherwise
	 */
	<T extends RootEntity> T get(Class<T> type, String refId);

	/**
	 * Tries to resolve the descriptor of the entity with the given type and ID.
	 * By default, this tries to resolve the entity and creates a descriptor of
	 * that entity but this should be overwritten when there is a more efficient
	 * way to do that for the respective implementation.
	 *
	 * @param type  the type of the requested entity
	 * @param refId the reference ID of the entity
	 * @param <T>   the type of the requested entity
	 * @return the requested descriptor if it could be resolved or {@code null}
	 * otherwise.
	 */
	default <T extends RootEntity> Descriptor getDescriptor(
		Class<T> type, String refId) {

		var t = get(type, refId);
		return t == null
			? null
			: Descriptor.of(t);
	}

	/**
	 * Get a category for the given model type and path.
	 *
	 * @param type the type of the category
	 * @param path the full path of the category
	 */
	Category getCategory(ModelType type, String path);

	void resolveProvider(String providerId, Exchange exchange);

	/**
	 * A simple default implementation that just returns {@code null} for every
	 * request.
	 */
	EntityResolver NULL = new EntityResolver() {
		@Override
		public <T extends RootEntity> T get(Class<T> type, String refId) {
			return null;
		}

		@Override
		public <T extends RootEntity> Descriptor getDescriptor(
			Class<T> type, String refId) {
			return null;
		}

		@Override
		public Category getCategory(ModelType type, String path) {
			return null;
		}

		@Override
		public void resolveProvider(String providerId, Exchange exchange) {
			if (exchange != null) {
				exchange.defaultProviderId = 0;
			}
		}
	};
}
