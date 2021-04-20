package org.openlca.proto.output;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class FlowWriter {

  private final WriterConfig config;

  public FlowWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Flow write(Flow flow) {
    var proto = Proto.Flow.newBuilder();
    if (flow == null)
      return proto.build();
    proto.setEntityType(EntityType.Flow);
    Out.map(flow, proto);
    Out.dep(config, flow.category);

    proto.setCas(Strings.orEmpty(flow.casNumber));
    proto.setFormula(Strings.orEmpty(flow.formula));
    proto.setInfrastructureFlow(flow.infrastructureFlow);
    proto.setSynonyms(Strings.orEmpty(flow.synonyms));
    proto.setFlowType(Out.flowTypeOf(flow.flowType));
    if (flow.location != null) {
      proto.setLocation(Refs.refOf(flow.location));
      Out.dep(config, flow.location);
    }
    writeFlowProperties(flow, proto);

    return proto.build();
  }

  private void writeFlowProperties(Flow flow, Proto.Flow.Builder proto) {
    for (var f : flow.flowPropertyFactors) {
      var protoF = Proto.FlowPropertyFactor.newBuilder();
      protoF.setConversionFactor(f.conversionFactor);
      if (f.flowProperty != null) {
        protoF.setFlowProperty(Refs.refOf(f.flowProperty));
        Out.dep(config, f.flowProperty);
        protoF.setReferenceFlowProperty(
          Objects.equals(f.flowProperty, flow.referenceFlowProperty));
      }
      proto.addFlowProperties(protoF.build());
    }
  }
}
