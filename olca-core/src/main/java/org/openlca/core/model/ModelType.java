package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
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

	final Class<? extends RefEntity> modelClass;

	ModelType(Class<? extends RootEntity> clazz) {
		this.modelClass = clazz;
	}

	public Class<? extends RefEntity> getModelClass() {
		return modelClass;
	}

	public boolean isRoot() {
		return modelClass != null
			&& RootEntity.class.isAssignableFrom(modelClass);
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

	public static ModelType[] rootTypes() {
		List<ModelType> list = new ArrayList<>();
		for (ModelType type : values()) {
			if (!type.isRoot())
				continue;
			list.add(type);
		}
		return list.toArray(new ModelType[0]);
	}

	/**
	 * Get the model type of the given entity.
	 *
	 * @param e a root entity; maybe {@code null}
	 * @return the model type of the entity or {@code null} if the type could
	 * not be determined or the entity was null.
	 */
	public static ModelType of(RootEntity e) {
		if (e == null)
			return null;
		for (var v : values()) {
			if (Objects.equals(e.getClass(), v.modelClass)) {
				return v;
			}
		}
		return null;
	}

}
