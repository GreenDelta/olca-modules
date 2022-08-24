package org.openlca.core.model;

import java.util.Objects;

/**
 * Enumeration of the root entity types in the openLCA domain model.
 */
public enum ModelType {

	PROJECT(Project.class),

	IMPACT_METHOD(ImpactMethod.class),

	IMPACT_CATEGORY(ImpactCategory.class),

	PRODUCT_SYSTEM(ProductSystem.class),

	PROCESS(Process.class),

	FLOW(Flow.class),

	FLOW_PROPERTY(FlowProperty.class),

	UNIT_GROUP(UnitGroup.class),

	ACTOR(Actor.class),

	SOURCE(Source.class),

	CATEGORY(Category.class),

	LOCATION(Location.class),

	SOCIAL_INDICATOR(SocialIndicator.class),

	CURRENCY(Currency.class),

	PARAMETER(Parameter.class),

	DQ_SYSTEM(DQSystem.class),

	RESULT(Result.class),

	EPD(Epd.class);

	final Class<? extends RootEntity> modelClass;

	ModelType(Class<? extends RootEntity> clazz) {
		this.modelClass = clazz;
	}

	public Class<? extends RootEntity> getModelClass() {
		return modelClass;
	}

	public static ModelType of(Class<? extends RootEntity> clazz) {
		if (clazz == null)
			return null;
		for (var type : values()) {
			if (Objects.equals(clazz, type.getModelClass()))
				return type;
		}
		return null;
	}

	/**
	 * Get the model type of the given entity.
	 */
	public static ModelType of(RootEntity e) {
		return e != null
				? of(e.getClass())
				: null;
	}

}
