package org.openlca.proto.output;

import java.time.Instant;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.Proto;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

public final class Out {

  private Out() {
  }

  public static Proto.Ref refOf(RootEntity e) {
    var proto = Proto.Ref.newBuilder();
    if (e == null)
      return proto.build();
    proto.setId(Strings.orEmpty(e.refId));
    proto.setName(Strings.orEmpty(e.name));
    proto.setDescription(Strings.orEmpty(e.description));
    proto.setVersion(Version.asString(e.version));
    proto.setType(e.getClass().getSimpleName());
    proto.setLastChange(dateTimeOf(e.lastChange));

    // add a the category path
    if (e instanceof CategorizedEntity) {
      var ce = (CategorizedEntity) e;
      if (ce.category != null) {
        var path = Categories.path(ce.category);
        if (!path.isEmpty()) {
          proto.addAllCategoryPath(path);
        }
      }
    }
    return proto.build();
  }

  public static Proto.Ref refOf(Descriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto.build();
    proto.setId(Strings.orEmpty(d.refId));
    proto.setName(Strings.orEmpty(d.name));
    proto.setDescription(Strings.orEmpty(d.description));
    proto.setVersion(Version.asString(d.version));
    proto.setLastChange(dateTimeOf(d.lastChange));

    // entity type
    if (d.type != null) {
      var type = d.type.getModelClass();
      if (type != null) {
        proto.setType(type.getSimpleName());
      }
    }

    return proto.build();
  }

  public static Proto.FlowRef.Builder flowRefOf(FlowDescriptor d) {
    var proto = Proto.FlowRef.newBuilder();
    if (d == null)
      return proto;
    proto.setId(Strings.orEmpty(d.refId));
    proto.setName(Strings.orEmpty(d.name));
    proto.setDescription(Strings.orEmpty(d.description));
    proto.setVersion(Version.asString(d.version));
    proto.setType("Flow");
    proto.setLastChange(dateTimeOf(d.lastChange));
    proto.setFlowType(flowTypeOf(d.flowType));
    return proto;
  }

  private static String dateTimeOf(long time) {
    return time == 0
      ? ""
      : Instant.ofEpochMilli(time).toString();
  }

  public static Proto.ProcessRef.Builder processRefOf(ProcessDescriptor d) {
    var proto = Proto.ProcessRef.newBuilder();
    if (d == null)
      return proto;
    return proto.setId(Strings.orEmpty(d.refId))
      .setName(Strings.orEmpty(d.name))
      .setDescription(Strings.orEmpty(d.description))
      .setVersion(Version.asString(d.version))
      .setType("Process")
      .setProcessType(processTypeOf(d.processType))
      .setLastChange(dateTimeOf(d.lastChange));
  }

  static Proto.Ref refOf(RootEntity e, WriterConfig config) {
    var proto = refOf(e);
    if (e == null)
      return proto;

    // push the dependency
    if (config != null && config.dependencies != null) {
      config.dependencies.push(e);
    }
    return proto;
  }

  static Proto.FlowRef flowRefOf(Flow flow, WriterConfig config) {

    var proto = Proto.FlowRef.newBuilder();
    if (flow == null)
      return proto.build();

    // push the dependency
    if (config != null && config.dependencies != null) {
      config.dependencies.push(flow);
    }

    proto.setId(Strings.orEmpty(flow.refId));
    proto.setName(Strings.orEmpty(flow.name));
    proto.setDescription(Strings.orEmpty(flow.description));
    proto.setVersion(Version.asString(flow.version));
    proto.setType("Flow");
    proto.setLastChange(dateTimeOf(flow.lastChange));

    // add a the category path
    if (flow.category != null) {
      var path = Categories.path(flow.category);
      if (!path.isEmpty()) {
        proto.addAllCategoryPath(path);
      }
    }

    // FlowRef specific fields
    if (flow.location != null) {
      proto.setLocation(Strings.orEmpty(flow.location.code));
    }
    var refUnit = flow.getReferenceUnit();
    if (refUnit != null) {
      proto.setRefUnit(Strings.orEmpty(refUnit.name));
    }
    proto.setFlowType(flowTypeOf(flow.flowType));

    return proto.build();
  }

  static Proto.ImpactCategoryRef impactRefOf(
    ImpactCategory impact, WriterConfig config) {

    var proto = Proto.ImpactCategoryRef.newBuilder();
    if (impact == null)
      return proto.build();

    // push the dependency
    if (config != null && config.dependencies != null) {
      config.dependencies.push(impact);
    }

    proto.setId(Strings.orEmpty(impact.refId));
    proto.setName(Strings.orEmpty(impact.name));
    proto.setDescription(Strings.orEmpty(impact.description));
    proto.setVersion(Version.asString(impact.version));
    proto.setType("ImpactCategory");
    proto.setLastChange(dateTimeOf(impact.lastChange));

    // add a the category path
    if (impact.category != null) {
      var path = Categories.path(impact.category);
      if (!path.isEmpty()) {
        proto.addAllCategoryPath(path);
      }
    }

    // ImpactCategoryRef specific fields
    proto.setRefUnit(Strings.orEmpty(impact.referenceUnit));

    return proto.build();
  }

  static Proto.FlowType flowTypeOf(FlowType type) {
    if (type == null)
      return Proto.FlowType.UNDEFINED_FLOW_TYPE;
    switch (type) {
      case ELEMENTARY_FLOW:
        return Proto.FlowType.ELEMENTARY_FLOW;
      case PRODUCT_FLOW:
        return Proto.FlowType.PRODUCT_FLOW;
      case WASTE_FLOW:
        return Proto.FlowType.WASTE_FLOW;
      default:
        return Proto.FlowType.UNDEFINED_FLOW_TYPE;
    }
  }

  static Proto.ProcessType processTypeOf(ProcessType type) {
    if (type == null)
      return Proto.ProcessType.UNDEFINED_PROCESS_TYPE;
    return type == ProcessType.LCI_RESULT
      ? Proto.ProcessType.LCI_RESULT
      : Proto.ProcessType.UNIT_PROCESS;
  }

  static Proto.Uncertainty uncertaintyOf(Uncertainty u) {
    var proto = Proto.Uncertainty.newBuilder();
    if (u == null || u.distributionType == null)
      return proto.build();

    // normal distribution
    if (u.distributionType == UncertaintyType.NORMAL) {
      proto.setDistributionType(
        Proto.UncertaintyType.NORMAL_DISTRIBUTION);

      if (u.parameter1 != null) {
        proto.setMean(u.parameter1);
      }
      if (u.formula1 != null) {
        proto.setMeanFormula(u.formula1);
      }

      if (u.parameter2 != null) {
        proto.setSd(u.parameter2);
      }
      if (u.formula2 != null) {
        proto.setSdFormula(u.formula2);
      }
    }

    // log-normal distribution
    if (u.distributionType == UncertaintyType.LOG_NORMAL) {
      proto.setDistributionType(
        Proto.UncertaintyType.LOG_NORMAL_DISTRIBUTION);

      if (u.parameter1 != null) {
        proto.setGeomMean(u.parameter1);
      }
      if (u.formula1 != null) {
        proto.setGeomMeanFormula(u.formula1);
      }

      if (u.parameter2 != null) {
        proto.setGeomSd(u.parameter2);
      }
      if (u.formula2 != null) {
        proto.setGeomSdFormula(u.formula2);
      }
    }

    // uniform distribution
    if (u.distributionType == UncertaintyType.UNIFORM) {
      proto.setDistributionType(
        Proto.UncertaintyType.UNIFORM_DISTRIBUTION);

      if (u.parameter1 != null) {
        proto.setMinimum(u.parameter1);
      }
      if (u.formula1 != null) {
        proto.setMinimumFormula(u.formula1);
      }

      if (u.parameter2 != null) {
        proto.setMaximum(u.parameter2);
      }
      if (u.formula2 != null) {
        proto.setMaximumFormula(u.formula2);
      }
    }

    // triangle distribution
    if (u.distributionType == UncertaintyType.TRIANGLE) {
      proto.setDistributionType(
        Proto.UncertaintyType.TRIANGLE_DISTRIBUTION);

      if (u.parameter1 != null) {
        proto.setMinimum(u.parameter1);
      }
      if (u.formula1 != null) {
        proto.setMinimumFormula(u.formula1);
      }

      if (u.parameter2 != null) {
        proto.setMode(u.parameter2);
      }
      if (u.formula2 != null) {
        proto.setModeFormula(u.formula2);
      }

      if (u.parameter3 != null) {
        proto.setMaximum(u.parameter3);
      }
      if (u.formula3 != null) {
        proto.setMaximumFormula(u.formula3);
      }
    }
    return proto.build();
  }
}
