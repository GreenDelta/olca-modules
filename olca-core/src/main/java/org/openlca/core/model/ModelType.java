package org.openlca.core.model;

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

	COST_CATEGORY(CostCategory.class),

	CURRENCY(Currency.class),

	PARAMETER(Parameter.class);

	final Class<?> modelClass;

	private ModelType(Class<?> clazz) {
		this.modelClass = clazz;
	}

	public Class<?> getModelClass() {
		return modelClass;
	}
	
	public boolean isCategorized() {
		if (modelClass == null)
			return false;
		return CategorizedEntity.class.isAssignableFrom(modelClass);
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

}
