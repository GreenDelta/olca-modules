package org.openlca.proto.io.output;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowPropertyFactor;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class FlowWriter {

  private final WriterConfig config;

  public FlowWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoFlow write(Flow flow) {
    var proto = ProtoFlow.newBuilder();
    if (flow == null)
      return proto.build();
    proto.setType(ProtoType.Flow);
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

  private void writeFlowProperties(Flow flow, ProtoFlow.Builder proto) {
    for (var f : flow.flowPropertyFactors) {
      var protoF = ProtoFlowPropertyFactor.newBuilder();
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
