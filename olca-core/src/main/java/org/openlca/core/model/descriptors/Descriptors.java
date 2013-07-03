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
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.results.ImpactResult;

public class Descriptors {

	public static BaseDescriptor toDescriptor(CategorizedEntity component) {
		if (component == null)
			return null;
		if (component instanceof Project)
			return toDescriptor((Project) component);
		if (component instanceof ImpactMethod)
			return toDescriptor((ImpactMethod) component);
		if (component instanceof ProductSystem)
			return toDescriptor((ProductSystem) component);
		if (component instanceof Process)
			return toDescriptor((Process) component);
		if (component instanceof Flow)
			return toDescriptor((Flow) component);
		if (component instanceof FlowProperty)
			return toDescriptor((FlowProperty) component);
		if (component instanceof UnitGroup)
			return toDescriptor((UnitGroup) component);
		if (component instanceof Actor)
			return toDescriptor((Actor) component);
		if (component instanceof Source)
			return toDescriptor((Source) component);
		if (component instanceof ImpactResult)
			return toDescriptor((ImpactResult) component);
		return createUnknownDescriptor(component);
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

	private static void setBaseValues(CategorizedEntity component,
			BaseDescriptor descriptor) {
		descriptor.setDescription(component.getDescription());
		descriptor.setId(component.getId());
		descriptor.setName(component.getName());
	}

	private static BaseDescriptor createUnknownDescriptor(
			CategorizedEntity component) {
		BaseDescriptor descriptor = new BaseDescriptor();
		descriptor.setDescription(component.getDescription());
		descriptor.setId(component.getId());
		descriptor.setName(component.getName());
		descriptor.setType(ModelType.UNKNOWN);
		return descriptor;
	}

}
