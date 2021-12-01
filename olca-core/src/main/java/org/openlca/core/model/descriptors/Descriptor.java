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
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

/**
 * Descriptors are lightweight models containing only descriptive information of
 * a corresponding entity. The intention of descriptors is to get this
 * information fast from the database without loading the complete model.
 * Therefore, the respective DAO classes should provide specific methods for
 * getting such descriptors.
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

	/**
	 * Tags are stored in a single string separated by commas `,`.
	 */
	public String tags;

	public ModelType type = ModelType.UNKNOWN;

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		var other = (Descriptor) obj;
		if (this.type != other.type)
			return false;
		if (this.id != 0 || other.id != 0)
			return this.id == other.id;
		return this.refId != null && this.refId.equals(other.refId);
	}

	@Override
	public int hashCode() {
		if (id != 0)
			return Long.hashCode(id);
		if (refId != null)
			return refId.hashCode();
		return super.hashCode();
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

	static Descriptor createUnknownDescriptor(RootEntity e) {
		var d = new Descriptor();
		setBaseValues(e, d);
		return d;
	}

	static CategorizedDescriptor createUnknownDescriptor(CategorizedEntity e) {
		return setBaseValues(e, new CategorizedDescriptor());
	}

	static <T extends Descriptor> T setBaseValues(RootEntity e, T d) {
		d.refId = e.refId;
		d.description = e.description;
		d.id = e.id;
		d.name = e.name;
		d.lastChange = e.lastChange;
		d.version = e.version;
		return d;
	}

	static <T extends CategorizedDescriptor> T setBaseValues(
		CategorizedEntity e, T d) {
		Descriptor.setBaseValues((RootEntity) e, d);
		if (e.category != null) {
			d.category = e.category.id;
		}
		d.library = e.library;
		d.tags = e.tags;
		return d;
	}

	public static DQSystemDescriptor of(DQSystem system) {
		return system == null
			? null
			: setBaseValues(system, new DQSystemDescriptor());
	}

	public static NwSetDescriptor of(NwSet nwSet) {
		if (nwSet == null)
			return null;
		var d = setBaseValues(nwSet, new NwSetDescriptor());
		d.weightedScoreUnit = nwSet.weightedScoreUnit;
		return d;
	}

	public static ImpactDescriptor of(ImpactCategory impact) {
		if (impact == null)
			return null;
		var d = setBaseValues(impact, new ImpactDescriptor());
		d.referenceUnit = impact.referenceUnit;
		return d;
	}

	public static CurrencyDescriptor of(Currency c) {
		return c == null
			? null
			: setBaseValues(c, new CurrencyDescriptor());
	}

	public static SocialIndicatorDescriptor of(SocialIndicator i) {
		return i == null
			? null
			: setBaseValues(i, new SocialIndicatorDescriptor());
	}

	public static ParameterDescriptor of(Parameter parameter) {
		return parameter == null
			? null
			: setBaseValues(parameter, new ParameterDescriptor());
	}

	public static LocationDescriptor of(Location location) {
		return location == null
			? null
			: setBaseValues(location, new LocationDescriptor());
	}

	public static SourceDescriptor of(Source source) {
		return source == null
			? null
			: setBaseValues(source, new SourceDescriptor());
	}

	public static ActorDescriptor of(Actor actor) {
		return actor == null
			? null
			: setBaseValues(actor, new ActorDescriptor());
	}

	public static UnitDescriptor of(Unit unit) {
		return unit == null
			? null
			: setBaseValues(unit, new UnitDescriptor());
	}

	public static UnitGroupDescriptor of(UnitGroup unitGroup) {
		return unitGroup == null
			? null
			: setBaseValues(unitGroup, new UnitGroupDescriptor());
	}

	public static FlowPropertyDescriptor of(FlowProperty flowProperty) {
		return flowProperty == null
			? null
			: setBaseValues(flowProperty, new FlowPropertyDescriptor());
	}

	public static FlowDescriptor of(Flow flow) {
		if (flow == null)
			return null;
		var d = setBaseValues(flow, new FlowDescriptor());
		if (flow.location != null)
			d.location = flow.location.id;
		var refProp = flow.referenceFlowProperty;
		if (refProp != null)
			d.refFlowPropertyId = refProp.id;
		d.flowType = flow.flowType;
		return d;
	}

	public static ProcessDescriptor of(Process process) {
		if (process == null)
			return null;
		var d = setBaseValues(process, new ProcessDescriptor());
		if (process.location != null)
			d.location = process.location.id;
		if (process.quantitativeReference != null)
			d.quantitativeReference = process.quantitativeReference.id;
		d.processType = process.processType;
		return d;
	}

	public static ProductSystemDescriptor of(ProductSystem system) {
		return system == null
			? null
			: setBaseValues(system, new ProductSystemDescriptor());
	}

	public static ImpactMethodDescriptor of(ImpactMethod method) {
		return method == null
			? null
			: setBaseValues(method, new ImpactMethodDescriptor());
	}

	public static ProjectDescriptor of(Project project) {
		return project == null
			? null
			: setBaseValues(project, new ProjectDescriptor());
	}

	public static CategoryDescriptor of(Category category) {
		if (category == null)
			return null;
		var d = setBaseValues(category, new CategoryDescriptor());
		d.categoryType = category.modelType;
		return d;
	}

	public static ResultDescriptor of(ResultModel result) {
		return result == null
			? null
			: setBaseValues(result, new ResultDescriptor());
	}

	public static CategorizedDescriptor of(CategorizedEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof Project)
			return of((Project) entity);
		if (entity instanceof ImpactCategory)
			return of((ImpactCategory) entity);
		if (entity instanceof ImpactMethod)
			return of((ImpactMethod) entity);
		if (entity instanceof ProductSystem)
			return of((ProductSystem) entity);
		if (entity instanceof Process)
			return of((Process) entity);
		if (entity instanceof Flow)
			return of((Flow) entity);
		if (entity instanceof FlowProperty)
			return of((FlowProperty) entity);
		if (entity instanceof UnitGroup)
			return of((UnitGroup) entity);
		if (entity instanceof Actor)
			return of((Actor) entity);
		if (entity instanceof Source)
			return of((Source) entity);
		if (entity instanceof SocialIndicator)
			return of((SocialIndicator) entity);
		if (entity instanceof Currency)
			return of((Currency) entity);
		if (entity instanceof Location)
			return of((Location) entity);
		if (entity instanceof Parameter)
			return of((Parameter) entity);
		if (entity instanceof Category)
			return of((Category) entity);
		if (entity instanceof DQSystem)
			return of((DQSystem) entity);
		if (entity instanceof ResultModel)
			return of((ResultModel) entity);
		return createUnknownDescriptor(entity);
	}

	public static Descriptor of(RootEntity entity) {
		if (entity == null)
			return null;
		if (entity instanceof CategorizedEntity)
			return of((CategorizedEntity) entity);
		if (entity instanceof NwSet)
			return of((NwSet) entity);
		if (entity instanceof Unit)
			return of((Unit) entity);
		return createUnknownDescriptor(entity);
	}

}
