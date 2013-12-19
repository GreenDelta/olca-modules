package org.openlca.core.database.usage;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;

/** Search of entities where another entity is used. */
public interface IUseSearch<T extends BaseDescriptor> {

	/**
	 * Returns a list of descriptors of entities where the given entity is used.
	 */
	public List<BaseDescriptor> findUses(T entity);

	public static final Factory FACTORY = new Factory();

	public class Factory {

		@SuppressWarnings("unchecked")
		public <T extends BaseDescriptor> IUseSearch<T> createFor(
				ModelType type, IDatabase database) {
			switch (type) {
			case ACTOR:
				return (IUseSearch<T>) new ActorUseSearch(database);
			case SOURCE:
				return (IUseSearch<T>) new SourceUseSearch(database);
			case UNIT_GROUP:
				return (IUseSearch<T>) new UnitGroupUseSearch(database);
			case FLOW_PROPERTY:
				return (IUseSearch<T>) new FlowPropertyUseSearch(database);
			case FLOW:
				return (IUseSearch<T>) new FlowUseSearch(database);
			case PROCESS:
				return (IUseSearch<T>) new ProcessUseSearch(database);
			case LOCATION:
				return (IUseSearch<T>) new LocationUseSearch(database);
			case IMPACT_METHOD:
				return (IUseSearch<T>) new ImpactMethodUseSearch(database);
			default:
				return new EmptyUseSearch<T>();
			}
		}
	}

}
