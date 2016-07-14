package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/** Search of entities where another entity is used. */
public interface IUseSearch<T extends CategorizedDescriptor> {

	/**
	 * Returns a list of descriptors of entities where the given entity id is
	 * used.
	 */
	public List<CategorizedDescriptor> findUses(long id);

	public List<CategorizedDescriptor> findUses(T entity);

	public List<CategorizedDescriptor> findUses(List<T> entity);

	public List<CategorizedDescriptor> findUses(Set<Long> entity);

	public static final Factory FACTORY = new Factory();

	public class Factory {

		@SuppressWarnings("unchecked")
		public <T extends CategorizedDescriptor> IUseSearch<T> createFor(
				ModelType type, IDatabase db) {
			switch (type) {
			case ACTOR:
				return (IUseSearch<T>) new ActorUseSearch(db);
			case SOURCE:
				return (IUseSearch<T>) new SourceUseSearch(db);
			case UNIT_GROUP:
				return (IUseSearch<T>) new UnitGroupUseSearch(db);
			case FLOW_PROPERTY:
				return (IUseSearch<T>) new FlowPropertyUseSearch(db);
			case FLOW:
				return (IUseSearch<T>) new FlowUseSearch(db);
			case PROCESS:
				return (IUseSearch<T>) new ProcessUseSearch(db);
			case PRODUCT_SYSTEM:
				return (IUseSearch<T>) new ProductSystemUseSearch(db);
			case LOCATION:
				return (IUseSearch<T>) new LocationUseSearch(db);
			case IMPACT_METHOD:
				return (IUseSearch<T>) new ImpactMethodUseSearch(db);
			case CURRENCY:
				return (IUseSearch<T>) new CurrencyUseSearch(db);
			case SOCIAL_INDICATOR:
				return (IUseSearch<T>) new SocialIndicatorUseSearch(db);
			case CATEGORY:
				return (IUseSearch<T>) new CategoryUseSearch(db);
			case PARAMETER: 
				return (IUseSearch<T>) new ParameterUseSearch(db);				
			case DQ_SYSTEM: 
				return (IUseSearch<T>) new DQSystemUseSearch(db);				
			default:
				return new EmptyUseSearch<T>();
			}
		}
	}

}
