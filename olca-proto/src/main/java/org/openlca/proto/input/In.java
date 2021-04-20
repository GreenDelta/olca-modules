package org.openlca.proto.input;

import java.util.Arrays;

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
import org.openlca.core.model.descriptors.CategorizedDescriptor;
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
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.openlca.jsonld.Enums;
import org.openlca.jsonld.Json;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

/**
 * Utility methods for converting incoming proto-objects to openLCA model
 * objects.
 */
public final class In {

  private In() {
  }

  public static Uncertainty uncertainty(Proto.Uncertainty proto) {
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

  public static AllocationMethod allocationMethod(Proto.AllocationType proto) {
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

  public static FlowType flowTypeOf(Proto.FlowType proto) {
    if (proto == null)
      return null;
    return switch (proto) {
      case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
      case PRODUCT_FLOW -> FlowType.PRODUCT_FLOW;
      case WASTE_FLOW -> FlowType.WASTE_FLOW;
      default -> null;
    };
  }

  public static ProcessType processTypeOf(Proto.ProcessType proto) {
    if (proto == null)
      return null;
    return switch (proto) {
      case LCI_RESULT -> ProcessType.LCI_RESULT;
      case UNIT_PROCESS -> ProcessType.UNIT_PROCESS;
      default -> null;
    };
  }

  public static ModelType modelTypeOf(Proto.ModelType proto) {
    return proto == null
      ? ModelType.UNKNOWN
      : Enums.getValue(proto.name(), ModelType.class);
  }

  public static Descriptor descriptorOf(Proto.Ref proto) {
    if (proto == null)
      return null;
    return fill(initDescriptor(proto), proto);
  }

  public static <T extends Descriptor> T fill(T d, Proto.Ref proto) {
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

  private static Descriptor initDescriptor(Proto.Ref ref) {
    return switch (ref.getEntityType()) {
      case Actor -> new ActorDescriptor();
      case Category -> new CategorizedDescriptor();
      case Currency -> new CurrencyDescriptor();
      case DQSystem -> new DQSystemDescriptor();
      case Flow -> new FlowDescriptor();
      case FlowProperty -> new FlowPropertyDescriptor();
      case ImpactCategory -> new ImpactDescriptor();
      case ImpactMethod -> new ImpactMethodDescriptor();
      case Location -> new LocationDescriptor();
      case Parameter -> new ParameterDescriptor();
      case Process -> new ProcessDescriptor();
      case ProductSystem -> new ProductSystemDescriptor();
      case Project -> new ProjectDescriptor();
      case SocialIndicator -> new SocialIndicatorDescriptor();
      case Source -> new SourceDescriptor();
      case UnitGroup -> new UnitDescriptor();
      case UNRECOGNIZED -> null;
      case Undefined -> new Descriptor();
    };
  }

  public static ParameterRedef parameterRedefOf(
    Proto.ParameterRedef proto, IDatabase db) {
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
