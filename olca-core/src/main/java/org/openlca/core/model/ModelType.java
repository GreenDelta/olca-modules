package org.openlca.core.model;

import org.openlca.core.model.results.LCIAResult;

/** Enumeration of the basic types in the openLCA domain model. */
public enum ModelType {

	UNKNOWN(null),

	PROJECT(Project.class),

	IMPACT_METHOD(LCIAMethod.class),

	IMPACT_CATEGORY(LCIACategory.class),

	PRODUCT_SYSTEM(ProductSystem.class),

	PROCESS(Process.class),

	FLOW(Flow.class),

	FLOW_PROPERTY(FlowProperty.class),

	UNIT_GROUP(UnitGroup.class),

	UNIT(Unit.class),

	ACTOR(Actor.class),

	SOURCE(Source.class),

	IMPACT_RESULT(LCIAResult.class);

	final Class<?> modelClass;

	private ModelType(Class<?> clazz) {
		this.modelClass = clazz;
	}

	public Class<?> getModelClass() {
		return modelClass;
	}

}
