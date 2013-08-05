package org.openlca.core.database.usage;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;

/** Search of entities where another entity is used. */
public interface IUseSearch<T> {

	/**
	 * Returns a list of descriptors of entities where the given entity is used.
	 */
	public List<BaseDescriptor> findUses(T entity);

	public static final Factory FACTORY = new Factory();

	public class Factory {

		@SuppressWarnings("unchecked")
		public <T> IUseSearch<T> createFor(Class<T> clazz, IDatabase database) {
			if (clazz == null)
				return null;
			if (clazz == Actor.class)
				return (IUseSearch<T>) new ActorUseSearch(database);
			if (clazz == Source.class)
				return (IUseSearch<T>) new SourceUseSearch(database);
			if (clazz == FlowProperty.class)
				return (IUseSearch<T>) new FlowPropertyUseSearch(database);
			if (clazz == Flow.class)
				return (IUseSearch<T>) new FlowUseSearch(database);
			if (clazz == Process.class)
				return (IUseSearch<T>) new ProcessUseSearch(database);
			if (clazz == Location.class)
				return (IUseSearch<T>) new LocationUseSearch(database);
			return null;
		}

	}

}
