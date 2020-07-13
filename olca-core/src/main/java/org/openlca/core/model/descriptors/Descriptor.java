package org.openlca.core.model.descriptors;

import java.util.Objects;

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
import org.openlca.core.model.ModelType;
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

/**
 * Descriptors are lightweight models containing only descriptive information of
 * a corresponding entity.The intention of descriptors is to get these
 * information fast from the database without loading the complete model.
 * Therefore, the respective DAO classes should provide these.
 */
public class Descriptor {

	public String refId;
	public long id;
	public String name;
	public String description;
	public long version;
	public long lastChange;

	/**
	 * Contains the library identifier if the corresponding data set of this
	 * descriptor belongs to a library. Otherwise this field is empty (or null).
	 */
	public String library;

	public ModelType type = ModelType.UNKNOWN;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(this.getClass().isInstance(obj)))
			return false;
		Descriptor other = (Descriptor) obj;
		if (this.type != other.type)
			return false;
		if (this.id != 0 || other.id != 0)
			return this.id == other.id;
		else
			return Objects.equals(this.refId, other.refId)
					&& Objects.equals(this.name, other.name)
					&& Objects.equals(this.description, other.description);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name
				+ ", type=" + type + "]";
	}

	/**
	 * Returns true if the data set of this descriptor is from a library.
	 */
	public boolean isFromLibrary() {
		return library != null;
	}

	static Descriptor createUnknownDescriptor(RootEntity entity) {
		Descriptor descriptor = new Descriptor();
		setBaseValues(entity, descriptor);
		return descriptor;
	}

	static CategorizedDescriptor createUnknownDescriptor(CategorizedEntity entity) {
		CategorizedDescriptor descriptor = new CategorizedDescriptor();
		setBaseValues(entity, descriptor);
		return descriptor;
	}

	static void setBaseValues(RootEntity entity, Descriptor descriptor) {
		descriptor.refId = entity.refId;
		descriptor.description = entity.description;
		descriptor.id = entity.id;
		descriptor.name = entity.name;
		descriptor.lastChange = entity.lastChange;
		descriptor.version = entity.version;
	}

	static void setBaseValues(CategorizedEntity entity, CategorizedDescriptor descriptor) {
		Descriptor.setBaseValues((RootEntity) entity, descriptor);
		if (entity.category != null)
			descriptor.category = entity.category.id;
	}

	public static DQSystemDescriptor of(DQSystem system) {
		if (system == null)
			return null;
		DQSystemDescriptor descriptor = new DQSystemDescriptor();
		Descriptor.setBaseValues(system, descriptor);
		return descriptor;
	}

	public static NwSetDescriptor of(NwSet nwSet) {
		if (nwSet == null)
			return null;
		NwSetDescriptor descriptor = new NwSetDescriptor();
		Descriptor.setBaseValues(nwSet, descriptor);
		descriptor.weightedScoreUnit = nwSet.weightedScoreUnit;
		return descriptor;
	}

	public static ImpactCategoryDescriptor of(ImpactCategory impact) {
		if (impact == null)
			return null;
		ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
		Descriptor.setBaseValues(impact, d);
		d.referenceUnit = impact.referenceUnit;
		return d;
	}

	public static CurrencyDescriptor of(Currency c) {
		if (c == null)
			return null;
		CurrencyDescriptor d = new CurrencyDescriptor();
		Descriptor.setBaseValues(c, d);
		return d;
	}

	public static SocialIndicatorDescriptor of(SocialIndicator i) {
		if (i == null)
			return null;
		SocialIndicatorDescriptor d = new SocialIndicatorDescriptor();
		Descriptor.setBaseValues(i, d);
		return d;
	}

	public static ParameterDescriptor of(Parameter parameter) {
		if (parameter == null)
			return null;
		ParameterDescriptor descriptor = new ParameterDescriptor();
		Descriptor.setBaseValues(parameter, descriptor);
		return descriptor;
	}

	public static LocationDescriptor of(Location location) {
		if (location == null)
			return null;
		LocationDescriptor descriptor = new LocationDescriptor();
		Descriptor.setBaseValues(location, descriptor);
		return descriptor;
	}

	public static SourceDescriptor of(Source source) {
		if (source == null)
			return null;
		SourceDescriptor descriptor = new SourceDescriptor();
		Descriptor.setBaseValues(source, descriptor);
		return descriptor;
	}

	public static ActorDescriptor of(Actor actor) {
		if (actor == null)
			return null;
		ActorDescriptor descriptor = new ActorDescriptor();
		Descriptor.setBaseValues(actor, descriptor);
		return descriptor;
	}

	public static UnitDescriptor of(Unit unit) {
		if (unit == null)
			return null;
		UnitDescriptor descriptor = new UnitDescriptor();
		Descriptor.setBaseValues(unit, descriptor);
		return descriptor;
	}

	public static UnitGroupDescriptor of(UnitGroup unitGroup) {
		if (unitGroup == null)
			return null;
		UnitGroupDescriptor descriptor = new UnitGroupDescriptor();
		Descriptor.setBaseValues(unitGroup, descriptor);
		return descriptor;
	}

	public static FlowPropertyDescriptor of(FlowProperty flowProperty) {
		if (flowProperty == null)
			return null;
		FlowPropertyDescriptor descriptor = new FlowPropertyDescriptor();
		Descriptor.setBaseValues(flowProperty, descriptor);
		return descriptor;
	}

	public static FlowDescriptor of(Flow flow) {
		if (flow == null)
			return null;
		FlowDescriptor descriptor = new FlowDescriptor();
		Descriptor.setBaseValues(flow, descriptor);
		if (flow.location != null)
			descriptor.location = flow.location.id;
		FlowProperty refProp = flow.referenceFlowProperty;
		if (refProp != null)
			descriptor.refFlowPropertyId = refProp.id;
		descriptor.flowType = flow.flowType;
		return descriptor;
	}

	public static ProcessDescriptor of(Process process) {
		if (process == null)
			return null;
		ProcessDescriptor descriptor = new ProcessDescriptor();
		Descriptor.setBaseValues(process, descriptor);
		if (process.location != null)
			descriptor.location = process.location.id;
		if (process.quantitativeReference != null)
			descriptor.quantitativeReference = process.quantitativeReference.id;
		descriptor.processType = process.processType;
		return descriptor;
	}

	public static ProductSystemDescriptor of(ProductSystem system) {
		if (system == null)
			return null;
		ProductSystemDescriptor descriptor = new ProductSystemDescriptor();
		Descriptor.setBaseValues(system, descriptor);
		return descriptor;
	}

	public static ImpactMethodDescriptor of(ImpactMethod method) {
		if (method == null)
			return null;
		ImpactMethodDescriptor descriptor = new ImpactMethodDescriptor();
		Descriptor.setBaseValues(method, descriptor);
		return descriptor;
	}

	public static ProjectDescriptor of(Project project) {
		if (project == null)
			return null;
		ProjectDescriptor descriptor = new ProjectDescriptor();
		Descriptor.setBaseValues(project, descriptor);
		return descriptor;
	}

	public static CategoryDescriptor of(Category category) {
		if (category == null)
			return null;
		CategoryDescriptor descriptor = new CategoryDescriptor();
		Descriptor.setBaseValues(category, descriptor);
		descriptor.categoryType = category.modelType;
		return descriptor;
	}

	public static CategorizedDescriptor of(CategorizedEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof Project)
			return Descriptor.of((Project) entity);
		if (entity instanceof ImpactCategory)
			return Descriptor.of((ImpactCategory) entity);
		if (entity instanceof ImpactMethod)
			return Descriptor.of((ImpactMethod) entity);
		if (entity instanceof ProductSystem)
			return Descriptor.of((ProductSystem) entity);
		if (entity instanceof Process)
			return Descriptor.of((Process) entity);
		if (entity instanceof Flow)
			return Descriptor.of((Flow) entity);
		if (entity instanceof FlowProperty)
			return Descriptor.of((FlowProperty) entity);
		if (entity instanceof UnitGroup)
			return Descriptor.of((UnitGroup) entity);
		if (entity instanceof Actor)
			return Descriptor.of((Actor) entity);
		if (entity instanceof Source)
			return Descriptor.of((Source) entity);
		if (entity instanceof SocialIndicator)
			return Descriptor.of((SocialIndicator) entity);
		if (entity instanceof Currency)
			return Descriptor.of((Currency) entity);
		if (entity instanceof Location)
			return Descriptor.of((Location) entity);
		if (entity instanceof Parameter)
			return Descriptor.of((Parameter) entity);
		if (entity instanceof Category)
			return Descriptor.of((Category) entity);
		if (entity instanceof DQSystem)
			return Descriptor.of((DQSystem) entity);
		return Descriptor.createUnknownDescriptor(entity);
	}

	public static Descriptor of(RootEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof CategorizedEntity)
			return Descriptor.of((CategorizedEntity) entity);
		if (entity instanceof NwSet)
			return Descriptor.of((NwSet) entity);
		if (entity instanceof Unit)
			return Descriptor.of((Unit) entity);
		return Descriptor.createUnknownDescriptor(entity);
	}

}
