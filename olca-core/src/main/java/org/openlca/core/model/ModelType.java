package org.openlca.core.model;

import org.openlca.core.model.results.ImpactResult;

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

	IMPACT_RESULT(ImpactResult.class),

	CATEGORY(Category.class),

	LOCATION(Location.class);

	final Class<?> modelClass;

	private ModelType(Class<?> clazz) {
		this.modelClass = clazz;
	}

	public Class<?> getModelClass() {
		return modelClass;
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
