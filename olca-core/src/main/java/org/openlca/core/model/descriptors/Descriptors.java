package org.openlca.core.model.descriptors;

import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class Descriptors {

	public static BaseDescriptor toDescriptor(RootEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof CategorizedEntity)
			return toDescriptor((CategorizedEntity) entity);
		if (entity instanceof ImpactCategory)
			return toDescriptor((ImpactCategory) entity);
		if (entity instanceof NwSet)
			return toDescriptor((NwSet) entity);
		if (entity instanceof Unit)
			return toDescriptor((Unit) entity);
		return createUnknownDescriptor(entity);
	}

	public static CategorizedDescriptor toDescriptor(CategorizedEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof Project)
			return toDescriptor((Project) entity);
		if (entity instanceof ImpactMethod)
			return toDescriptor((ImpactMethod) entity);
		if (entity instanceof ProductSystem)
			return toDescriptor((ProductSystem) entity);
		if (entity instanceof Process)
			return toDescriptor((Process) entity);
		if (entity instanceof Flow)
			return toDescriptor((Flow) entity);
		if (entity instanceof FlowProperty)
			return toDescriptor((FlowProperty) entity);
		if (entity instanceof UnitGroup)
			return toDescriptor((UnitGroup) entity);
		if (entity instanceof Actor)
			return toDescriptor((Actor) entity);
		if (entity instanceof Source)
			return toDescriptor((Source) entity);
		if (entity instanceof SocialIndicator)
			return toDescriptor((SocialIndicator) entity);
		if (entity instanceof Currency)
			return toDescriptor((Currency) entity);
		if (entity instanceof Location)
			return toDescriptor((Location) entity);
		if (entity instanceof Parameter)
			return toDescriptor((Parameter) entity);
		if (entity instanceof Category)
			return toDescriptor((Category) entity);
		if (entity instanceof DQSystem)
			return toDescriptor((DQSystem) entity);
		return createUnknownDescriptor(entity);
	}

	public static CategoryDescriptor toDescriptor(Category category) {
		if (category == null)
			return null;
		CategoryDescriptor descriptor = new CategoryDescriptor();
		setBaseValues(category, descriptor);
		descriptor.setCategoryType(category.getModelType());
		return descriptor;
	}

	public static ProjectDescriptor toDescriptor(Project project) {
		if (project == null)
			return null;
		ProjectDescriptor descriptor = new ProjectDescriptor();
		setBaseValues(project, descriptor);
		return descriptor;
	}

	public static ImpactMethodDescriptor toDescriptor(ImpactMethod method) {
		if (method == null)
			return null;
		ImpactMethodDescriptor descriptor = new ImpactMethodDescriptor();
		setBaseValues(method, descriptor);
		return descriptor;
	}

	public static ProductSystemDescriptor toDescriptor(ProductSystem system) {
		if (system == null)
			return null;
		ProductSystemDescriptor descriptor = new ProductSystemDescriptor();
		setBaseValues(system, descriptor);
		return descriptor;
	}

	public static ProcessDescriptor toDescriptor(Process process) {
		if (process == null)
			return null;
		ProcessDescriptor descriptor = new ProcessDescriptor();
		setBaseValues(process, descriptor);
		if (process.getLocation() != null)
			descriptor.setLocation(process.getLocation().getId());
		if (process.getQuantitativeReference() != null)
			descriptor.setQuantitativeReference(process.getQuantitativeReference().getId());
		descriptor.setProcessType(process.getProcessType());
		return descriptor;
	}

	public static FlowDescriptor toDescriptor(Flow flow) {
		if (flow == null)
			return null;
		FlowDescriptor descriptor = new FlowDescriptor();
		setBaseValues(flow, descriptor);
		if (flow.getLocation() != null)
			descriptor.setLocation(flow.getLocation().getId());
		FlowProperty refProp = flow.getReferenceFlowProperty();
		if (refProp != null)
			descriptor.setRefFlowPropertyId(refProp.getId());
		descriptor.setFlowType(flow.getFlowType());
		return descriptor;
	}

	public static FlowPropertyDescriptor toDescriptor(FlowProperty flowProperty) {
		if (flowProperty == null)
			return null;
		FlowPropertyDescriptor descriptor = new FlowPropertyDescriptor();
		setBaseValues(flowProperty, descriptor);
		return descriptor;
	}

	public static UnitGroupDescriptor toDescriptor(UnitGroup unitGroup) {
		if (unitGroup == null)
			return null;
		UnitGroupDescriptor descriptor = new UnitGroupDescriptor();
		setBaseValues(unitGroup, descriptor);
		return descriptor;
	}

	public static UnitDescriptor toDescriptor(Unit unit) {
		if (unit == null)
			return null;
		UnitDescriptor descriptor = new UnitDescriptor();
		setBaseValues(unit, descriptor);
		return descriptor;
	}

	public static ActorDescriptor toDescriptor(Actor actor) {
		if (actor == null)
			return null;
		ActorDescriptor descriptor = new ActorDescriptor();
		setBaseValues(actor, descriptor);
		return descriptor;
	}

	public static SourceDescriptor toDescriptor(Source source) {
		if (source == null)
			return null;
		SourceDescriptor descriptor = new SourceDescriptor();
		setBaseValues(source, descriptor);
		return descriptor;
	}

	public static LocationDescriptor toDescriptor(Location location) {
		if (location == null)
			return null;
		LocationDescriptor descriptor = new LocationDescriptor();
		setBaseValues(location, descriptor);
		return descriptor;
	}

	public static ParameterDescriptor toDescriptor(Parameter parameter) {
		if (parameter == null)
			return null;
		ParameterDescriptor descriptor = new ParameterDescriptor();
		setBaseValues(parameter, descriptor);
		return descriptor;
	}

	public static SocialIndicatorDescriptor toDescriptor(SocialIndicator i) {
		if (i == null)
			return null;
		SocialIndicatorDescriptor d = new SocialIndicatorDescriptor();
		setBaseValues(i, d);
		return d;
	}

	public static CurrencyDescriptor toDescriptor(Currency c) {
		if (c == null)
			return null;
		CurrencyDescriptor d = new CurrencyDescriptor();
		setBaseValues(c, d);
		return d;
	}

	public static ImpactCategoryDescriptor toDescriptor(ImpactCategory impactCategory) {
		if (impactCategory == null)
			return null;
		ImpactCategoryDescriptor descriptor = new ImpactCategoryDescriptor();
		setBaseValues(impactCategory, descriptor);
		descriptor.setReferenceUnit(impactCategory.referenceUnit);
		return descriptor;
	}

	public static NwSetDescriptor toDescriptor(NwSet nwSet) {
		if (nwSet == null)
			return null;
		NwSetDescriptor descriptor = new NwSetDescriptor();
		setBaseValues(nwSet, descriptor);
		descriptor.setWeightedScoreUnit(nwSet.weightedScoreUnit);
		return descriptor;
	}

	public static DQSystemDescriptor toDescriptor(DQSystem system) {
		if (system == null)
			return null;
		DQSystemDescriptor descriptor = new DQSystemDescriptor();
		setBaseValues(system, descriptor);
		return descriptor;
	}

	private static void setBaseValues(CategorizedEntity entity, CategorizedDescriptor descriptor) {
		setBaseValues((RootEntity) entity, descriptor);
		if (entity.getCategory() != null)
			descriptor.setCategory(entity.getCategory().getId());
	}

	private static void setBaseValues(RootEntity entity, BaseDescriptor descriptor) {
		descriptor.setRefId(entity.getRefId());
		descriptor.setDescription(entity.getDescription());
		descriptor.setId(entity.getId());
		descriptor.setName(entity.getName());
		descriptor.setLastChange(entity.getLastChange());
		descriptor.setVersion(entity.getVersion());
	}

	private static CategorizedDescriptor createUnknownDescriptor(CategorizedEntity entity) {
		CategorizedDescriptor descriptor = new CategorizedDescriptor();
		setBaseValues(entity, descriptor);
		return descriptor;
	}

	private static BaseDescriptor createUnknownDescriptor(RootEntity entity) {
		BaseDescriptor descriptor = new BaseDescriptor();
		setBaseValues(entity, descriptor);
		return descriptor;
	}

}
