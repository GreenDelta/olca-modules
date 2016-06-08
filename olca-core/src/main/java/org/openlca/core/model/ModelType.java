package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

/** Enumeration of the basic types in the openLCA domain model. */
public enum ModelType {

	UNKNOWN(null),

	PROJECT(Project.class),

	IMPACT_METHOD(ImpactMethod.class),

	IMPACT_CATEGORY(ImpactCategory.class),

	PRODUCT_SYSTEM(ProductSystem.class),

	PROCESS(Process.class),

	FLOW(Flow.class),

	FLOW_PROPERTY(FlowProperty.class),

	UNIT_GROUP(UnitGroup.class),

	UNIT(Unit.class),

	ACTOR(Actor.class),

	SOURCE(Source.class),

	CATEGORY(Category.class),

	LOCATION(Location.class),

	NW_SET(NwSet.class),

	SOCIAL_INDICATOR(SocialIndicator.class),

	CURRENCY(Currency.class),

	PARAMETER(Parameter.class),
	
	DQ_SYSTEM(DQSystem.class);

	final Class<? extends AbstractEntity> modelClass;

	private ModelType(Class<? extends AbstractEntity> clazz) {
		this.modelClass = clazz;
	}

	public Class<? extends AbstractEntity> getModelClass() {
		return modelClass;
	}

	public boolean isCategorized() {
		if (modelClass == null)
			return false;
		return CategorizedEntity.class.isAssignableFrom(modelClass);
	}

	public boolean isOneOf(ModelType... types) {
		if (types == null || types.length == 0)
			return false;
		for (ModelType type : types)
			if (this == type)
				return true;
		return false;
	}

	public static ModelType forModelClass(Class<?> clazz) {
		if (clazz == null)
			return null;
		for (ModelType type : ModelType.values()) {
			if (clazz.equals(type.getModelClass()))
				return type;
		}
		return null;
	}

	public static ModelType[] categorized() {
		List<ModelType> categorized = new ArrayList<>();
		for (ModelType type : values()) {
			if (!type.isCategorized())
				continue;
			categorized.add(type);
		}
		return categorized.toArray(new ModelType[categorized.size()]);
	}

}
