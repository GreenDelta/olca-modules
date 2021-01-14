package org.openlca.proto.input;

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
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.Enums;
import org.openlca.jsonld.Json;
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
    switch (proto.getDistributionType()) {
      case LOG_NORMAL_DISTRIBUTION:
        return Uncertainty.logNormal(
          proto.getGeomMean(), proto.getGeomSd());
      case NORMAL_DISTRIBUTION:
        return Uncertainty.normal(
          proto.getMean(), proto.getSd());
      case TRIANGLE_DISTRIBUTION:
        return Uncertainty.triangle(
          proto.getMinimum(), proto.getMode(), proto.getMaximum());
      case UNIFORM_DISTRIBUTION:
        return Uncertainty.uniform(
          proto.getMinimum(), proto.getMaximum());
      default:
        return null;
    }
  }

  public static AllocationMethod allocationMethod(Proto.AllocationType proto) {
    if (proto == null)
      return null;
    switch (proto) {
      case CAUSAL_ALLOCATION:
        return AllocationMethod.CAUSAL;
      case ECONOMIC_ALLOCATION:
        return AllocationMethod.ECONOMIC;
      case PHYSICAL_ALLOCATION:
        return AllocationMethod.PHYSICAL;
      case USE_DEFAULT_ALLOCATION:
        return AllocationMethod.USE_DEFAULT;
      default:
        return AllocationMethod.NONE;
    }
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
    switch (proto) {
      case ELEMENTARY_FLOW:
        return FlowType.ELEMENTARY_FLOW;
      case PRODUCT_FLOW:
        return FlowType.PRODUCT_FLOW;
      case WASTE_FLOW:
        return FlowType.WASTE_FLOW;
      default:
        return null;
    }
  }

  public static ProcessType processTypeOf(Proto.ProcessType proto) {
    return proto == Proto.ProcessType.LCI_RESULT
      ? ProcessType.LCI_RESULT
      : ProcessType.UNIT_PROCESS;
  }

  public static ModelType modelTypeOf(Proto.ModelType proto) {
    return proto == null
      ? ModelType.UNKNOWN
      : Enums.getValue(proto.name(), ModelType.class);
  }

  public static FlowDescriptor descriptorOf(Proto.FlowRef proto) {
    if (proto == null)
      return null;
    var d = new FlowDescriptor();
    d.refId = proto.getId();
    d.flowType = flowTypeOf(proto.getFlowType());
    d.name = proto.getName();
    d.description = Strings.orNull(proto.getDescription());
    d.lastChange = timeOf(proto.getLastChange());
    d.version = versionOf(proto.getVersion());
    return d;
  }

  public static ProcessDescriptor descriptorOf(Proto.ProcessRef proto) {
    if (proto == null)
      return null;
    var d = new ProcessDescriptor();
    d.refId = proto.getId();
    d.processType = processTypeOf(proto.getProcessType());
    d.name = proto.getName();
    d.description = Strings.orNull(proto.getDescription());
    d.lastChange = timeOf(proto.getLastChange());
    d.version = versionOf(proto.getVersion());
    return d;
  }

  public static Descriptor descriptorOf(Proto.Ref proto) {
    if (proto == null)
      return null;
    var d = new Descriptor();
    d.refId = proto.getId();
    d.name = proto.getName();
    d.description = Strings.orNull(proto.getDescription());
    d.lastChange = timeOf(proto.getLastChange());
    d.version = versionOf(proto.getVersion());

    // try to determine the model type
    try {
      var type = proto.getType();
      if (Strings.notEmpty(type)) {
        var clazz = Class.forName(
          "org.openlca.core.model." + type);
        d.type = ModelType.forModelClass(clazz);
      }
    } catch (Exception ignored) {
    }

    return d;
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
