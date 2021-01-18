package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

public final class Out {

  private Out() {
  }

  static void dep(WriterConfig config, RootEntity e) {
    if (config == null
      || config.dependencies == null
      || e == null)
      return;
    config.dependencies.push(e);
  }

  static void dep(WriterConfig config, Descriptor d) {
    if (config == null
      || config.dependencies == null
      || d == null)
      return;
    config.dependencies.push(d);
  }

  public static Proto.Ref.Builder refOf(RootEntity e) {
    var proto = Proto.Ref.newBuilder();
    if (e == null)
      return proto;
    map(e, proto);
    return proto;
  }

  public static Proto.Ref.Builder refOf(Descriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    map(d, proto);
    return proto;
  }

  /**
   * Creates a Ref that just contains the reference ID of the given descriptor.
   * This is useful when we have a huge amount of references and we know what
   * type they contain.
   */
  public static Proto.Ref.Builder tinyRefOf(Descriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    proto.setId(Strings.orEmpty(d.refId));
    return proto;
  }

  public static Proto.Ref.Builder flowRefOf(FlowDescriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    map(d, proto);
    proto.setFlowType(flowTypeOf(d.flowType));
    return proto;
  }

  private static String dateTimeOf(long time) {
    return time == 0
      ? ""
      : Instant.ofEpochMilli(time).toString();
  }

  public static Proto.Ref.Builder processRefOf(ProcessDescriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    map(d, proto);
    proto.setProcessType(processTypeOf(d.processType));
    return proto;
  }

  public static Proto.Ref.Builder processRefOf(Process p) {
    var proto = Proto.Ref.newBuilder();
    if (p == null)
      return proto;
    map(p, proto);
    proto.setProcessType(processTypeOf(p.processType));
    return proto;
  }

  /**
   * Map the common entity fields to the proto object.
   */
  static void map(RootEntity e, Message.Builder proto) {
    if (e == null || proto == null)
      return;
    var fields = proto.getDescriptorForType().getFields();
    for (var field : fields) {
      switch (field.getName()) {
        case "id":
          set(proto, field, e.refId);
          break;
        case "type":
          set(proto, field, e.getClass().getSimpleName());
          break;
        case "name":
          set(proto, field, e.name);
          break;
        case "description":
          set(proto, field, e.description);
          break;
        case "version":
          if (e.version != 0) {
            set(proto, field, Version.asString(e.version));
          }
          break;
        case "lastChange":
          if (e.lastChange != 0) {
            set(proto, field, dateTimeOf(e.lastChange));
          }
          break;

        case "library":
          if (e instanceof CategorizedEntity) {
            var ce = (CategorizedEntity) e;
            set(proto, field, ce.library);
          }
          break;

        case "category":
          if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (e instanceof CategorizedEntity) {
              var ce = (CategorizedEntity) e;
              if (ce.category != null) {
                var catRef = Out.refOf(ce.category);
                proto.setField(field, catRef.build());
              }
            }
          }
          break;

        case "tags":
          if (e instanceof CategorizedEntity) {
            var ce = (CategorizedEntity) e;
            if (Strings.notEmpty(ce.tags)) {
              var tags = Arrays.stream(ce.tags.split(","))
                .filter(Strings::notEmpty)
                .collect(Collectors.toList());
              setRepeated(proto, field, tags);
            }
          }
          break;

        case "categoryPath":
          if (e instanceof CategorizedEntity) {
            var ce = (CategorizedEntity) e;
            if (ce.category != null) {
              var path = Categories.path(ce.category);
              setRepeated(proto, field, path);
            }
          }
          break;
      }
    }
  }

  private static void map(Descriptor d, Message.Builder proto) {
    if (d == null || proto == null)
      return;
    var fields = proto.getDescriptorForType().getFields();
    for (var field : fields) {
      switch (field.getName()) {
        case "id":
          set(proto, field, d.refId);
          break;
        case "type":
          if (d.type != null) {
            var modelClass = d.type.getModelClass();
            if (modelClass != null) {
              set(proto, field, modelClass.getSimpleName());
            }
          }
          break;
        case "name":
          set(proto, field, d.name);
          break;
        case "description":
          set(proto, field, d.description);
          break;
        case "version":
          set(proto, field, Version.asString(d.version));
          break;
        case "lastChange":
          set(proto, field, dateTimeOf(d.lastChange));
          break;
        case "library":
          set(proto, field, d.library);
          break;
      }
    }
  }

  private static void set(Message.Builder proto,
                          Descriptors.FieldDescriptor field,
                          String value) {
    if (Strings.nullOrEmpty(value))
      return;
    if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.STRING)
      return;
    proto.setField(field, value);
  }

  private static void setRepeated(Message.Builder proto,
                                  Descriptors.FieldDescriptor field,
                                  List<String> values) {
    if (values == null || values.isEmpty())
      return;
    if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.STRING)
      return;
    for (var value : values) {
      proto.addRepeatedField(field, value);
    }
  }

  static Proto.Ref.Builder flowRefOf(Flow flow) {
    var proto = Proto.Ref.newBuilder();
    if (flow == null)
      return proto;

    map(flow, proto);

    // FlowRef specific fields
    if (flow.location != null) {
      proto.setLocation(Strings.orEmpty(flow.location.code));
    }
    var refUnit = flow.getReferenceUnit();
    if (refUnit != null) {
      proto.setRefUnit(Strings.orEmpty(refUnit.name));
    }
    proto.setFlowType(flowTypeOf(flow.flowType));

    return proto;
  }

  static Proto.Ref.Builder impactRefOf(ImpactCategory impact) {
    var proto = Proto.Ref.newBuilder();
    if (impact == null)
      return proto;
    map(impact, proto);
    proto.setRefUnit(Strings.orEmpty(impact.referenceUnit));
    return proto;
  }

  public static Proto.Ref.Builder impactRefOf(ImpactDescriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    map(d, proto);
    proto.setRefUnit(Strings.orEmpty(d.referenceUnit));
    return proto;
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
