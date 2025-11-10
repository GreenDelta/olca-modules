package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonObject;

public interface EntityReader<T extends RootEntity> {

	T read(JsonObject json);

	void update(T entity, JsonObject json);

	@SuppressWarnings("unchecked")
	static <T extends RootEntity> EntityReader<T> of(
			Class<T> type, EntityResolver resolver
	) {
		if (Objects.equals(type, Actor.class))
			return (EntityReader<T>) new ActorReader(resolver);
		if (Objects.equals(type, Currency.class))
			return (EntityReader<T>) new CurrencyReader(resolver);
		if (Objects.equals(type, DQSystem.class))
			return (EntityReader<T>) new DQSystemReader(resolver);
		if (Objects.equals(type, Epd.class))
			return (EntityReader<T>) new EpdReader(resolver);
		if (Objects.equals(type, Flow.class))
			return (EntityReader<T>) new FlowReader(resolver);
		if (Objects.equals(type, FlowProperty.class))
			return (EntityReader<T>) new FlowPropertyReader(resolver);
		if (Objects.equals(type, ImpactCategory.class))
			return (EntityReader<T>) new ImpactCategoryReader(resolver);
		if (Objects.equals(type, ImpactMethod.class))
			return (EntityReader<T>) new ImpactMethodReader(resolver);
		if (Objects.equals(type, Location.class))
			return (EntityReader<T>) new LocationReader(resolver);
		if (Objects.equals(type, Parameter.class))
			return (EntityReader<T>) new ParameterReader(resolver);
		if (Objects.equals(type, Process.class))
			return (EntityReader<T>) new ProcessReader(resolver);
		if (Objects.equals(type, ProductSystem.class))
			return (EntityReader<T>) new ProductSystemReader(resolver);
		if (Objects.equals(type, Project.class))
			return (EntityReader<T>) new ProjectReader(resolver);
		if (Objects.equals(type, Result.class))
			return (EntityReader<T>) new ResultReader(resolver);
		if (Objects.equals(type, SocialIndicator.class))
			return (EntityReader<T>) new SocialIndicatorReader(resolver);
		if (Objects.equals(type, Source.class))
			return (EntityReader<T>) new SourceReader(resolver);
		if (Objects.equals(type, UnitGroup.class))
			return (EntityReader<T>) new UnitGroupReader(resolver);
		return null;
	}

}
