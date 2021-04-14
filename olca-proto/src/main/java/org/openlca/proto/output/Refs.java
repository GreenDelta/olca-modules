package org.openlca.proto.output;

import com.google.protobuf.Message;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

/**
 * A utility class for creating data set references.
 */
public final class Refs {

  private Refs() {
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

  public static Proto.Ref.Builder refOf(RootEntity e) {
    var proto = Proto.Ref.newBuilder();
    if (e == null)
      return proto;
    Out.map(e, proto);

    if (e instanceof Flow) {

      // flow specific fields
      var flow = (Flow) e;
      if (flow.flowType != null) {
        proto.setFlowType(Out.flowTypeOf(flow.flowType));
      }
      if (flow.location != null) {
        proto.setLocation(Strings.orEmpty(flow.location.code));
      }
      var refUnit = flow.getReferenceUnit();
      if (refUnit != null) {
        proto.setRefUnit(Strings.orEmpty(refUnit.name));
      }

      // process specific fields
    } else if (e instanceof Process) {
      var process = (Process) e;
      if (process.processType != null) {
        proto.setProcessType(Out.processTypeOf(process.processType));
      }
      if (process.location != null) {
        proto.setLocation(Strings.orEmpty(process.location.code));
      }

      // impact specific fields
    } else if (e instanceof ImpactCategory) {
      proto.setRefUnit(Strings.orEmpty(
        ((ImpactCategory) e).referenceUnit));
    }
    return proto;
  }

  public static Proto.Ref.Builder refOf(Descriptor d) {
    var proto = Proto.Ref.newBuilder();
    if (d == null)
      return proto;
    map(d, proto);
    if (d instanceof FlowDescriptor) {
      var fd = (FlowDescriptor) d;
      if (fd.flowType != null) {
        proto.setFlowType(Out.flowTypeOf(fd.flowType));
      }
    } else if (d instanceof ProcessDescriptor) {
      var pd = (ProcessDescriptor) d;
      if (pd.processType != null) {
        proto.setProcessType(Out.processTypeOf(pd.processType));
      }
    } else if (d instanceof ImpactDescriptor) {
      var id = (ImpactDescriptor) d;
      proto.setRefUnit(Strings.orEmpty(id.referenceUnit));
    }
    return proto;
  }

  private static void map(Descriptor d, Message.Builder proto) {
    if (d == null || proto == null)
      return;
    var fields = proto.getDescriptorForType().getFields();
    for (var field : fields) {
      switch (field.getName()) {
        case "id":
          Out.set(proto, field, d.refId);
          break;
        case "type":
          if (d.type != null) {
            var modelClass = d.type.getModelClass();
            if (modelClass != null) {
              Out.set(proto, field, modelClass.getSimpleName());
            }
          }
          break;
        case "name":
          Out.set(proto, field, d.name);
          break;
        case "description":
          Out.set(proto, field, d.description);
          break;
        case "version":
          Out.set(proto, field, Version.asString(d.version));
          break;
        case "lastChange":
          Out.set(proto, field, Out.dateTimeOf(d.lastChange));
          break;
        case "library":
          Out.set(proto, field, d.library);
          break;
      }
    }
  }
}
