package org.openlca.core.io;

import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * This interface abstracts away the loading of entities from some data store.
 * Implementations could be for example databases, caches, imports, or
 * combinations of that.
 */
public interface EntityResolver {

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
	<T extends RefEntity> T get(Class<T> type, String refId);

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
	default <T extends RefEntity> Descriptor getDescriptor(
		Class<T> type, String refId) {

		var t = get(type, refId);
		return t == null
			? null
			: Descriptor.of(t);
	}

	/**
	 * A simple default implementation that just returns {@code null} for every
	 * request.
	 */
	EntityResolver NULL = new EntityResolver() {
		@Override
		public <T extends RefEntity> T get(Class<T> type, String refId) {
			return null;
		}

		@Override
		public <T extends RefEntity> Descriptor getDescriptor(
			Class<T> type, String refId) {
			return null;
		}
	};
}
