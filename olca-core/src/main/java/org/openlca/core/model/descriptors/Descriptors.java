package org.openlca.core.model.descriptors;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.results.ImpactResult;

public class Descriptors {

	public static RootEntity toModel(BaseDescriptor descriptor) {
		if (descriptor == null)
			return null;
		switch (descriptor.getModelType()) {
		case ACTOR:
			return toActor(descriptor);
		case SOURCE:
			return toSource(descriptor);
		case UNIT_GROUP:
			return toUnitGroup(descriptor);
		case FLOW_PROPERTY:
			return toFlowProperty(descriptor);
		case FLOW:
			return toFlow(descriptor);
		case PROCESS:
			return toProcess(descriptor);
		case PRODUCT_SYSTEM:
			return toProductSystem(descriptor);
		case PROJECT:
			return toProject(descriptor);
		case IMPACT_METHOD:
			return toImpactMethod(descriptor);
		case IMPACT_RESULT:
			return toImpactResult(descriptor);
		case CATEGORY:
			return toCategory(descriptor);
		case UNKNOWN:
		default:
			return null;
		}
	}

	public static Actor toActor(BaseDescriptor descriptor) {
		Actor actor = new Actor();
		setBaseValues(descriptor, actor);
		return actor;
	}

	public static Source toSource(BaseDescriptor descriptor) {
		Source source = new Source();
		setBaseValues(descriptor, source);
		return source;
	}

	public static UnitGroup toUnitGroup(BaseDescriptor descriptor) {
		UnitGroup unitGroup = new UnitGroup();
		setBaseValues(descriptor, unitGroup);
		return unitGroup;
	}

	public static FlowProperty toFlowProperty(BaseDescriptor descriptor) {
		FlowProperty flowProperty = new FlowProperty();
		setBaseValues(descriptor, flowProperty);
		return flowProperty;
	}

	public static Flow toFlow(BaseDescriptor descriptor) {
		Flow flow = new Flow();
		setBaseValues(descriptor, flow);
		return flow;
	}

	public static Process toProcess(BaseDescriptor descriptor) {
		Process process = new Process();
		setBaseValues(descriptor, process);
		return process;
	}

	public static ProductSystem toProductSystem(BaseDescriptor descriptor) {
		ProductSystem productSystem = new ProductSystem();
		setBaseValues(descriptor, productSystem);
		return productSystem;
	}

	public static Project toProject(BaseDescriptor descriptor) {
		Project project = new Project();
		setBaseValues(descriptor, project);
		return project;
	}

	public static ImpactMethod toImpactMethod(BaseDescriptor descriptor) {
		ImpactMethod impactMethod = new ImpactMethod();
		setBaseValues(descriptor, impactMethod);
		return impactMethod;
	}

	public static ImpactResult toImpactResult(BaseDescriptor descriptor) {
		ImpactResult impactResult = new ImpactResult();
		setBaseValues(descriptor, impactResult);
		return impactResult;
	}

	public static Category toCategory(BaseDescriptor descriptor) {
		Category category = new Category();
		setBaseValues(descriptor, category);
		return category;
	}

	private static void setBaseValues(BaseDescriptor descriptor,
			RootEntity entity) {
		entity.setId(descriptor.getId());
		entity.setName(descriptor.getName());
		entity.setDescription(descriptor.getDescription());
		entity.setRefId(descriptor.getRefId());
	}

	public static BaseDescriptor toDescriptor(RootEntity entity) {
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
		if (entity instanceof ImpactResult)
			return toDescriptor((ImpactResult) entity);
		if (entity instanceof Category)
			return toDescriptor((Category) entity);
		return createUnknownDescriptor(entity);
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
			descriptor.setLocationCode(process.getLocation().getCode());
		return descriptor;
	}

	public static FlowDescriptor toDescriptor(Flow flow) {
		if (flow == null)
			return null;
		FlowDescriptor descriptor = new FlowDescriptor();
		setBaseValues(flow, descriptor);
		if (flow.getLocation() != null)
			descriptor.setLocationCode(flow.getLocation().getCode());
		if (flow.getFlowType() != null)
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

	public static BaseDescriptor toDescriptor(ImpactResult impactResult) {
		if (impactResult == null)
			return null;
		BaseDescriptor descriptor = new BaseDescriptor();
		descriptor.setType(ModelType.IMPACT_RESULT);
		setBaseValues(impactResult, descriptor);
		return descriptor;
	}

	public static ImpactCategoryDescriptor toDescriptor(
			ImpactCategory impactCategory) {
		if (impactCategory == null)
			return null;
		ImpactCategoryDescriptor descriptor = new ImpactCategoryDescriptor();
		descriptor.setType(ModelType.IMPACT_CATEGORY);
		descriptor.setDescription(impactCategory.getDescription());
		descriptor.setId(impactCategory.getId());
		descriptor.setName(impactCategory.getName());
		descriptor.setReferenceUnit(impactCategory.getReferenceUnit());
		return descriptor;
	}

	public static CategoryDescriptor toDescriptor(Category category) {
		if (category == null)
			return null;
		CategoryDescriptor descriptor = new CategoryDescriptor();
		descriptor.setType(ModelType.CATEGORY);
		setBaseValues(category, descriptor);
		String path = category.getName();
		if (category.getParentCategory() != null)
			path = category.getParentCategory().getName() + "/" + path;
		descriptor.setShortPath(path);
		return descriptor;
	}

	private static void setBaseValues(RootEntity entity,
			BaseDescriptor descriptor) {
		descriptor.setRefId(entity.getRefId());
		descriptor.setDescription(entity.getDescription());
		descriptor.setId(entity.getId());
		descriptor.setName(entity.getName());
	}

	private static BaseDescriptor createUnknownDescriptor(RootEntity component) {
		BaseDescriptor descriptor = new BaseDescriptor();
		descriptor.setDescription(component.getDescription());
		descriptor.setId(component.getId());
		descriptor.setName(component.getName());
		descriptor.setType(ModelType.UNKNOWN);
		return descriptor;
	}

}
