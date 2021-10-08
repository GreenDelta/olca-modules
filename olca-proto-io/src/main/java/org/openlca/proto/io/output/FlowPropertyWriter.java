package org.openlca.proto.io.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoFlowPropertyType;
import org.openlca.proto.ProtoType;

public class FlowPropertyWriter {

  private final WriterConfig config;

  public FlowPropertyWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoFlowProperty write(FlowProperty property) {
    var proto = ProtoFlowProperty.newBuilder();
    if (property == null)
      return proto.build();
    proto.setType(ProtoType.FlowProperty);
    Out.map(property, proto);
    Out.dep(config, property.category);

    // model specific fields
    writeFlowPropertyType(property, proto);
    if (property.unitGroup != null) {
      proto.setUnitGroup(Refs.refOf(property.unitGroup));
      Out.dep(config, property.unitGroup);
    }

    return proto.build();
  }

  private void writeFlowPropertyType(
    FlowProperty property, ProtoFlowProperty.Builder proto) {
    if (property.flowPropertyType == null)
      return;
    switch (property.flowPropertyType) {
      case PHYSICAL -> proto.setFlowPropertyType(
        ProtoFlowPropertyType.PHYSICAL_QUANTITY);
      case ECONOMIC -> proto.setFlowPropertyType(
        ProtoFlowPropertyType.ECONOMIC_QUANTITY);
    }
  }
}
