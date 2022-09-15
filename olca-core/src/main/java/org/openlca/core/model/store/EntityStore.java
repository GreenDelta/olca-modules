package org.openlca.core.model.store;

import gnu.trove.set.TLongSet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface EntityStore {

	/**
	 * Inserts the given entity in the data store. A new ID is allocated for
	 * the entity be the data store. The returned instance is the same instance
	 * as passed in as parameter but with an updated ID.
	 *
	 * @param e the entity that should be inserted into the data store
	 * @return the same entity but with an updated ID.
	 */
	<T extends RootEntity> T insert(T e);

	/**
	 * Inserts multiple entities in the data store.
	 */
	default void insert(RootEntity e1, RootEntity e2, RootEntity... more) {
		insert(e1);
		insert(e2);
		if (more == null)
			return;
		for (var e : more) {
			insert(e);
		}
	}

	/**
	 * Updates the given entity in the data store. The entity must already be
	 * present in this data store. The returned instance may be a different
	 * instance than the parameter instance and reflects the merged state.
	 *
	 * @param e the entity that should be updated
	 * @return the updated entity, may be a different instance
	 */
	<T extends RootEntity> T update(T e);

	<T extends RootEntity> void delete(T e);

	default void delete(RootEntity e1, RootEntity e2, RootEntity... more) {
		this.delete(e1);
		this.delete(e2);
		if (more == null)
			return;
		for (var e : more) {
			delete(e);
		}
	}

	<T extends RootEntity> T get(Class<T> type, long id);

	default <T extends RootEntity> List<T> getAll(Class<T> type, TLongSet ids) {
		if (type == null || ids == null || ids.isEmpty())
			return Collections.emptyList();
		var list = new ArrayList<T>(ids.size());
		for (var it = ids.iterator(); it.hasNext(); ) {
			long id = it.next();
			var e = get(type, id);
			if (e != null) {
				list.add(e);
			}
		}
		return list;
	}

	<T extends RootEntity> T get(Class<T> type, String refId);

	<T extends RootEntity> RootDescriptor getDescriptor(Class<T> type, long id);

	/**
	 * Get the descriptor of the entity of the given type and reference ID.
	 */
	<T extends RootEntity> RootDescriptor getDescriptor(
			Class<T> type, String refID);

	/**
	 * Get all entities of the given type from this data store.
	 */
	<T extends RootEntity> List<T> getAll(Class<T> type);

	/**
	 * Get the descriptors of all entities of the given type from this data store.
	 */
	<T extends RootEntity> List<? extends RootDescriptor> getDescriptors(Class<T> type);

	default <T extends RootEntity> List<? extends RootDescriptor> getDescriptors(
			Class<T> type, TLongSet ids) {
		var list = new ArrayList<RootDescriptor>(ids.size());
		for (var i = ids.iterator(); i.hasNext(); ) {
			var d = getDescriptor(type, i.next());
			if (d != null) {
				list.add(d);
			}
		}
		return list;
	}

	/**
	 * Get the first entity of the given type and with the given name from the
	 * data store. It returns {@code null} when no entity with the given name
	 * exists.
	 */
	<T extends RootEntity> T getForName(Class<T> type, String name);

	/**
	 * Deletes everything from this data store.
	 */
	void clear();
}
