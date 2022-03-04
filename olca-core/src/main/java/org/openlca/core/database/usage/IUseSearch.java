package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;

/** Search of entities where another entity is used. */
public interface IUseSearch<T extends RootDescriptor> {

	default List<RootDescriptor> findUses(long id) {
		TLongSe
	}

	List<RootDescriptor> findUses(T entity);

	List<RootDescriptor> findUses(List<T> entity);

	List<RootDescriptor> findUses(Set<Long> entity);

	Factory FACTORY = new Factory();

	class Factory {

		@SuppressWarnings("unchecked")
		public <T extends RootDescriptor> IUseSearch<T> createFor(
				ModelType type, IDatabase db) {
			return switch (type) {
				case ACTOR -> (IUseSearch<T>) new ActorUseSearch(db);
				case SOURCE -> (IUseSearch<T>) new SourceUseSearch(db);
				case UNIT_GROUP -> (IUseSearch<T>) new UnitGroupUseSearch(db);
				case FLOW_PROPERTY -> (IUseSearch<T>) new FlowPropertyUseSearch(db);
				case FLOW -> (IUseSearch<T>) new FlowUseSearch(db);
				case PROCESS -> (IUseSearch<T>) new ProcessUseSearch(db);
				case PRODUCT_SYSTEM -> (IUseSearch<T>) new ProductSystemUseSearch(db);
				case LOCATION -> (IUseSearch<T>) new LocationUseSearch(db);
				case IMPACT_CATEGORY -> (IUseSearch<T>) new ImpactCategoryUseSearch(db);
				case IMPACT_METHOD -> (IUseSearch<T>) new ImpactMethodUseSearch(db);
				case CURRENCY -> (IUseSearch<T>) new CurrencyUseSearch(db);
				case SOCIAL_INDICATOR -> (IUseSearch<T>) new SocialIndicatorUseSearch(db);
				case CATEGORY -> (IUseSearch<T>) new CategoryUseSearch(db);
				case PARAMETER -> (IUseSearch<T>) new ParameterUseSearch(db);
				case DQ_SYSTEM -> (IUseSearch<T>) new DQSystemUseSearch(db);
				default -> new EmptyUseSearch<>();
			};
		}
	}

}
