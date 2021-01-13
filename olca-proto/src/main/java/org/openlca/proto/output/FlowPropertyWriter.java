package org.openlca.proto.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.proto.generated.Proto;

public class FlowPropertyWriter {

  private final WriterConfig config;

  public FlowPropertyWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.FlowProperty write(FlowProperty property) {
    var proto = Proto.FlowProperty.newBuilder();
    if (property == null)
      return proto.build();
    Out.map(property, proto);
    Out.dep(config, property.category);

    // model specific fields
    writeFlowPropertyType(property, proto);
    if (property.unitGroup != null) {
      proto.setUnitGroup(Out.refOf(property.unitGroup));
      Out.dep(config, property.unitGroup);
    }

    return proto.build();
  }

  private void writeFlowPropertyType(
    FlowProperty property, Proto.FlowProperty.Builder proto) {
    if (property.flowPropertyType == null)
      return;
    switch (property.flowPropertyType) {
      case PHYSICAL:
        proto.setFlowPropertyType(
          Proto.FlowPropertyType.PHYSICAL_QUANTITY);
        break;
      case ECONOMIC:
        proto.setFlowPropertyType(
          Proto.FlowPropertyType.ECONOMIC_QUANTITY);
        break;
    }
  }
}
