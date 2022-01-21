package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of the basic types in the openLCA domain model.
 */
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

	DQ_SYSTEM(DQSystem.class),

	RESULT(Result.class);

	final Class<? extends RootEntity> modelClass;

	ModelType(Class<? extends RootEntity> clazz) {
		this.modelClass = clazz;
	}

	public Class<? extends RootEntity> getModelClass() {
		return modelClass;
	}

	public boolean isCategorized() {
		return modelClass != null
			&& CategorizedEntity.class.isAssignableFrom(modelClass);
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
		return categorized.toArray(new ModelType[0]);
	}

	/**
	 * Get the model type of the given entity.
	 *
	 * @param e a root entity; maybe {@code null}
	 * @return the model type of the entity or {@code UNKNOWN} if the type could
	 * not be determined or the entity was null.
	 */
	public static ModelType of(RootEntity e) {
		if (e == null)
			return UNKNOWN;
		for (var v : values()) {
			if (v.modelClass == null)
				continue;
			if (e.getClass().equals(v.modelClass)) {
				return v;
			}
		}
		return UNKNOWN;
	}

}
