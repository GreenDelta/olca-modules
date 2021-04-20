package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
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

  static String dateTimeOf(long time) {
    return time == 0
      ? ""
      : Instant.ofEpochMilli(time).toString();
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
        case "last_change":
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
                var catRef = Refs.refOf(ce.category);
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

        case "category_path":
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

  static void set(
    Message.Builder proto, Descriptors.FieldDescriptor field, String value) {
    if (Strings.nullOrEmpty(value))
      return;
    if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.STRING)
      return;
    proto.setField(field, value);
  }

  private static void setRepeated(
    Message.Builder proto,
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

  public static Proto.ModelType modelTypeOf(ModelType type) {
    if (type == null)
      return Proto.ModelType.UNDEFINED_MODEL_TYPE;
    return switch (type) {
      case UNKNOWN -> Proto.ModelType.UNDEFINED_MODEL_TYPE;
      case PROJECT -> Proto.ModelType.PROJECT;
      case IMPACT_METHOD -> Proto.ModelType.IMPACT_METHOD;
      case IMPACT_CATEGORY -> Proto.ModelType.IMPACT_CATEGORY;
      case PRODUCT_SYSTEM -> Proto.ModelType.PRODUCT_SYSTEM;
      case PROCESS -> Proto.ModelType.PROCESS;
      case FLOW -> Proto.ModelType.FLOW;
      case FLOW_PROPERTY -> Proto.ModelType.FLOW_PROPERTY;
      case UNIT_GROUP -> Proto.ModelType.UNIT_GROUP;
      case UNIT -> Proto.ModelType.UNIT;
      case ACTOR -> Proto.ModelType.ACTOR;
      case SOURCE -> Proto.ModelType.SOURCE;
      case CATEGORY -> Proto.ModelType.CATEGORY;
      case LOCATION -> Proto.ModelType.LOCATION;
      case NW_SET -> Proto.ModelType.NW_SET;
      case SOCIAL_INDICATOR -> Proto.ModelType.SOCIAL_INDICATOR;
      case CURRENCY -> Proto.ModelType.CURRENCY;
      case PARAMETER -> Proto.ModelType.PARAMETER;
      case DQ_SYSTEM -> Proto.ModelType.DQ_SYSTEM;
    };
  }

  static Proto.FlowType flowTypeOf(FlowType type) {
    if (type == null)
      return Proto.FlowType.UNDEFINED_FLOW_TYPE;
    return switch (type) {
      case ELEMENTARY_FLOW -> Proto.FlowType.ELEMENTARY_FLOW;
      case PRODUCT_FLOW -> Proto.FlowType.PRODUCT_FLOW;
      case WASTE_FLOW -> Proto.FlowType.WASTE_FLOW;
    };
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

  public static AbstractMessage toProto(IDatabase db, RootEntity e) {

    var conf = WriterConfig.of(db);

    if (e instanceof Actor)
      return new ActorWriter(conf).write((Actor) e);

    if (e instanceof Category)
      return new CategoryWriter(conf).write((Category) e);

    if (e instanceof Currency)
      return new CurrencyWriter(conf).write((Currency) e);

    if (e instanceof DQSystem)
      return new DQSystemWriter(conf).write((DQSystem) e);

    if (e instanceof Flow)
      return new FlowWriter(conf).write((Flow) e);

    if (e instanceof FlowProperty)
      return new FlowPropertyWriter(conf).write((FlowProperty) e);

    if (e instanceof ImpactCategory)
      return new ImpactCategoryWriter(conf).write((ImpactCategory) e);

    if (e instanceof ImpactMethod)
      return new ImpactMethodWriter(conf).write((ImpactMethod) e);

    if (e instanceof Location)
      return new LocationWriter(conf).write((Location) e);

    if (e instanceof Parameter)
      return new ParameterWriter(conf).write((Parameter) e);

    if (e instanceof Process)
      return new ProcessWriter(conf).write((Process) e);

    if (e instanceof ProductSystem)
      return new ProductSystemWriter(conf).write((ProductSystem) e);

    if (e instanceof Project)
      return new ProjectWriter(conf).write((Project) e);

    if (e instanceof SocialIndicator)
      return new SocialIndicatorWriter(conf).write((SocialIndicator) e);

    if (e instanceof Source)
      return new SourceWriter(conf).write((Source) e);

    if (e instanceof UnitGroup)
      return new UnitGroupWriter(conf).write((UnitGroup) e);

    throw new RuntimeException(
      "Unsupported entity type" + " for binary translation: " + e.getClass());
  }
}
