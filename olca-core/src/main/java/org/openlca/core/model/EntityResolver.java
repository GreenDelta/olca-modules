package org.openlca.core.model;

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
	 * @return the requested entity if it could be resolved or {@code null} if no
	 * such entity could be resolved.
	 */
	<T extends RootEntity> T get(Class<T> type, String refId);

	/**
	 * A simple default implementation that just returns {@code null} for every
	 * request.
	 */
	EntityResolver NULL = new EntityResolver() {
		@Override
		public <T extends RootEntity> T get(Class<T> type, String refId) {
			return null;
		}
	};

}
