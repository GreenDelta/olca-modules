package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.TLongSets;

public interface IUseSearch {

	default Set<? extends RootDescriptor> find(long id) {
		return find(TLongSets.singleton(id));
	}

	Set<? extends RootDescriptor> find(TLongSet ids);

	static IUseSearch of(ModelType type, IDatabase db) {
		if (type == null)
			return new EmptyUseSearch();

		return switch (type) {
			case ACTOR -> new ActorUseSearch(db);
			case SOURCE -> new SourceUseSearch(db);
			case UNIT_GROUP -> new UnitGroupUseSearch(db);
			case FLOW_PROPERTY -> new FlowPropertyUseSearch(db);
			case FLOW -> new FlowUseSearch(db);
			case PROCESS -> new ProcessUseSearch(db);
			case PRODUCT_SYSTEM -> new ProductSystemUseSearch(db);
			case LOCATION -> new LocationUseSearch(db);
			case IMPACT_CATEGORY -> new ImpactCategoryUseSearch(db);
			case IMPACT_METHOD -> new ImpactMethodUseSearch(db);
			case CURRENCY -> new CurrencyUseSearch(db);
			case SOCIAL_INDICATOR -> new SocialIndicatorUseSearch(db);
			case CATEGORY -> new CategoryUseSearch(db);
			case PARAMETER -> new ParameterUseSearch(db);
			case DQ_SYSTEM -> new DQSystemUseSearch(db);
			case RESULT -> new EmptyUseSearch(); // TODO
			case EPD, PROJECT, UNIT, NW_SET, UNKNOWN -> new EmptyUseSearch(); // TODO
		};
	}

}
