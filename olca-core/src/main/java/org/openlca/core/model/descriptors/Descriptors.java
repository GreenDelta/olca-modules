package org.openlca.core.model.descriptors;

import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
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

public class Descriptors {

	public static BaseDescriptor toDescriptor(RootEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof CategorizedEntity)
			return toDescriptor((CategorizedEntity) entity);
		if (entity instanceof ImpactCategory)
			return toDescriptor((ImpactCategory) entity);
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
			descriptor.setLocation(process.getLocation().getId());
		return descriptor;
	}

	public static FlowDescriptor toDescriptor(Flow flow) {
		if (flow == null)
			return null;
		FlowDescriptor descriptor = new FlowDescriptor();
		setBaseValues(flow, descriptor);
		if (flow.getLocation() != null)
			descriptor.setLocation(flow.getLocation().getId());
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

	public static Class<? extends RootEntity> getModelClass(
			Class<? extends BaseDescriptor> descriptorClass) {
		if (descriptorClass == BaseDescriptor.class)
			return RootEntity.class;
		else if (descriptorClass == CategorizedDescriptor.class)
			return CategorizedEntity.class;
		else if (descriptorClass == ActorDescriptor.class)
			return Actor.class;
		else if (descriptorClass == SourceDescriptor.class)
			return Source.class;
		else if (descriptorClass == UnitGroupDescriptor.class)
			return UnitGroup.class;
		else if (descriptorClass == FlowPropertyDescriptor.class)
			return FlowProperty.class;
		else if (descriptorClass == FlowDescriptor.class)
			return Flow.class;
		else if (descriptorClass == ProcessDescriptor.class)
			return Process.class;
		else if (descriptorClass == ProductSystemDescriptor.class)
			return ProductSystem.class;
		else if (descriptorClass == ImpactMethodDescriptor.class)
			return ImpactMethod.class;
		else if (descriptorClass == ImpactCategoryDescriptor.class)
			return ImpactCategory.class;
		return RootEntity.class;
	}

	private static void setBaseValues(CategorizedEntity entity,
			CategorizedDescriptor descriptor) {
		setBaseValues((RootEntity) entity, descriptor);
		if (entity.getCategory() != null)
			descriptor.setCategory(entity.getCategory().getId());
	}

	private static void setBaseValues(RootEntity entity,
			BaseDescriptor descriptor) {
		descriptor.setRefId(entity.getRefId());
		descriptor.setDescription(entity.getDescription());
		descriptor.setId(entity.getId());
		descriptor.setName(entity.getName());
	}

	private static CategorizedDescriptor createUnknownDescriptor(
			CategorizedEntity entity) {
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
