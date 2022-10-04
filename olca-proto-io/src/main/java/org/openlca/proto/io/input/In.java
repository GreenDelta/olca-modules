package org.openlca.proto.io.input;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.EpdDescriptor;
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
import org.openlca.proto.ProtoFlowType;
import org.openlca.proto.ProtoProcessType;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;
import org.openlca.proto.ProtoUncertainty;

/**
 * Utility methods for converting incoming proto-objects to openLCA model
 * objects.
 */
public final class In {

  private In() {
  }

  public static ModelType modelTypeOf(ProtoType protoType) {
    if (protoType == null)
      return null;
    return switch (protoType) {
      case Actor -> ModelType.ACTOR;
      case Currency -> ModelType.CURRENCY;
      case DQSystem -> ModelType.DQ_SYSTEM;
			case Epd -> ModelType.EPD;
      case Flow -> ModelType.FLOW;
      case FlowProperty -> ModelType.FLOW_PROPERTY;
      case ImpactCategory -> ModelType.IMPACT_CATEGORY;
      case ImpactMethod -> ModelType.IMPACT_METHOD;
      case Location -> ModelType.LOCATION;
      case Parameter -> ModelType.PARAMETER;
      case Process -> ModelType.PROCESS;
      case ProductSystem -> ModelType.PRODUCT_SYSTEM;
      case Project -> ModelType.PROJECT;
      case Result -> ModelType.RESULT;
      case SocialIndicator -> ModelType.SOCIAL_INDICATOR;
      case Source -> ModelType.SOURCE;
      case UnitGroup -> ModelType.UNIT_GROUP;
      default -> null;
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
      case Currency -> new CurrencyDescriptor();
      case DQSystem -> new DQSystemDescriptor();
			case Epd -> new EpdDescriptor();
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

}
