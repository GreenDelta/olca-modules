package org.openlca.proto.io.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;
import org.openlca.core.model.descriptors.DQSystemDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.openlca.core.model.descriptors.ResultDescriptor;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.proto.ProtoAllocationType;
import org.openlca.proto.ProtoFlowType;
import org.openlca.proto.ProtoParameterRedef;
import org.openlca.proto.ProtoProcessType;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;
import org.openlca.proto.ProtoUncertainty;
import org.openlca.util.Strings;

/**
 * Utility methods for converting incoming proto-objects to openLCA model
 * objects.
 */
public final class In {

  private In() {
  }

  public static ModelType modelTypeOf(ProtoType protoType) {
    if (protoType == null)
      return ModelType.UNKNOWN;
    return switch (protoType) {
      case Actor -> ModelType.ACTOR;
      case Category -> ModelType.CATEGORY;
      case Currency -> ModelType.CURRENCY;
      case DQSystem -> ModelType.DQ_SYSTEM;
      case Flow -> ModelType.FLOW;
      case FlowProperty -> ModelType.FLOW_PROPERTY;
      case ImpactCategory -> ModelType.IMPACT_CATEGORY;
      case ImpactMethod -> ModelType.IMPACT_METHOD;
      case Location -> ModelType.LOCATION;
      case NwSet -> ModelType.NW_SET;
      case Parameter -> ModelType.PARAMETER;
      case Process -> ModelType.PROCESS;
      case ProductSystem -> ModelType.PRODUCT_SYSTEM;
      case Project -> ModelType.PROJECT;
      case Result -> ModelType.RESULT;
      case SocialIndicator -> ModelType.SOCIAL_INDICATOR;
      case Source -> ModelType.SOURCE;
      case Unit -> ModelType.UNIT;
      case UnitGroup -> ModelType.UNIT_GROUP;
      case Undefined, UNRECOGNIZED -> ModelType.UNKNOWN;
    };
  }

  public static Uncertainty uncertainty(ProtoUncertainty proto) {
    if (proto == null)
      return null;
    return switch (proto.getDistributionType()) {
      case LOG_NORMAL_DISTRIBUTION -> Uncertainty.logNormal(
        proto.getGeomMean(), proto.getGeomSd());
      case NORMAL_DISTRIBUTION -> Uncertainty.normal(
        proto.getMean(), proto.getSd());
      case TRIANGLE_DISTRIBUTION -> Uncertainty.triangle(
        proto.getMinimum(), proto.getMode(), proto.getMaximum());
      case UNIFORM_DISTRIBUTION -> Uncertainty.uniform(
        proto.getMinimum(), proto.getMaximum());
      default -> null;
    };
  }

  public static AllocationMethod allocationMethod(ProtoAllocationType proto) {
    if (proto == null)
      return null;
    return switch (proto) {
      case CAUSAL_ALLOCATION -> AllocationMethod.CAUSAL;
      case ECONOMIC_ALLOCATION -> AllocationMethod.ECONOMIC;
      case PHYSICAL_ALLOCATION -> AllocationMethod.PHYSICAL;
      case USE_DEFAULT_ALLOCATION -> AllocationMethod.USE_DEFAULT;
      default -> AllocationMethod.NONE;
    };
  }

  public static long timeOf(String dateTime) {
    if (Strings.nullOrEmpty(dateTime))
      return 0;
    var date = Json.parseDate(dateTime);
    return date == null
      ? 0
      : date.getTime();
  }

  public static long versionOf(String version) {
    return Strings.nullOrEmpty(version)
      ? 0
      : Version.fromString(version).getValue();
  }

  public static FlowType flowTypeOf(ProtoFlowType proto) {
    if (proto == null)
      return null;
    return switch (proto) {
      case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
      case PRODUCT_FLOW -> FlowType.PRODUCT_FLOW;
      case WASTE_FLOW -> FlowType.WASTE_FLOW;
      default -> null;
    };
  }

  public static ProcessType processTypeOf(ProtoProcessType proto) {
    if (proto == null)
      return null;
    return switch (proto) {
      case LCI_RESULT -> ProcessType.LCI_RESULT;
      case UNIT_PROCESS -> ProcessType.UNIT_PROCESS;
      default -> null;
    };
  }

  public static Descriptor descriptorOf(ProtoRef proto) {
    if (proto == null)
      return null;
    return fill(initDescriptor(proto), proto);
  }

  public static <T extends Descriptor> T fill(T d, ProtoRef proto) {
    if (d == null || proto == null)
      return null;
    d.refId = proto.getId();
    d.name = proto.getName();
    d.description = Strings.nullIfEmpty(proto.getDescription());
    d.lastChange = timeOf(proto.getLastChange());
    d.version = versionOf(proto.getVersion());
    d.library = proto.getLibrary();

    if (d instanceof ProcessDescriptor) {
      ((ProcessDescriptor) d).processType = processTypeOf(
        proto.getProcessType());
    }

    if (d instanceof FlowDescriptor) {
      ((FlowDescriptor) d).flowType = flowTypeOf(
        proto.getFlowType());
    }

    return d;
  }

  private static Descriptor initDescriptor(ProtoRef ref) {
    return switch (ref.getType()) {
      case Actor -> new ActorDescriptor();
      case Category -> new RootDescriptor();
      case Currency -> new CurrencyDescriptor();
      case DQSystem -> new DQSystemDescriptor();
      case Flow -> new FlowDescriptor();
      case FlowProperty -> new FlowPropertyDescriptor();
      case ImpactCategory -> new ImpactDescriptor();
      case ImpactMethod -> new ImpactMethodDescriptor();
      case Location -> new LocationDescriptor();
      case NwSet -> new NwSetDescriptor();
      case Parameter -> new ParameterDescriptor();
      case Process -> new ProcessDescriptor();
      case ProductSystem -> new ProductSystemDescriptor();
      case Project -> new ProjectDescriptor();
      case SocialIndicator -> new SocialIndicatorDescriptor();
      case Source -> new SourceDescriptor();
      case Unit -> new UnitDescriptor();
      case UnitGroup -> new UnitGroupDescriptor();
      case Result -> new ResultDescriptor();
      case UNRECOGNIZED, Undefined -> new Descriptor();
    };
  }

  public static ParameterRedef parameterRedefOf(
    ProtoParameterRedef proto, IDatabase db) {
    var redef = new ParameterRedef();
    if (proto == null)
      return redef;
    redef.name = proto.getName();
    redef.value = proto.getValue();
    redef.uncertainty = In.uncertainty(proto.getUncertainty());
    redef.description = proto.getDescription();

    // context
    var context = proto.getContext().getId();
    if (Strings.nullOrEmpty(context))
      return redef;

    // we could check the context type but do we know that
    // this is correctly entered? thus, we first try to
    // find a process with that ID (the usual case) and
    // then an impact category
    var process = db.getDescriptor(Process.class, context);
    if (process != null) {
      redef.contextType = ModelType.PROCESS;
      redef.contextId = process.id;
      return redef;
    }

    var impact = db.getDescriptor(ImpactCategory.class, context);
    if (impact == null)
      return redef;
    redef.contextType = ModelType.IMPACT_CATEGORY;
    redef.contextId = impact.id;
    return redef;
  }

}
