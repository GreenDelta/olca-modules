package org.openlca.proto.io.output;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowPropertyFactor;
import org.openlca.proto.ProtoType;
import org.openlca.commons.Strings;

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

    proto.setCas(Strings.notNull(flow.casNumber));
    proto.setFormula(Strings.notNull(flow.formula));
    proto.setIsInfrastructureFlow(flow.infrastructureFlow);
    proto.setSynonyms(Strings.notNull(flow.synonyms));
    proto.setFlowType(Out.flowTypeOf(flow.flowType));
		config.dep(flow.location, proto::setLocation);
    writeFlowProperties(flow, proto);

    return proto.build();
  }

  private void writeFlowProperties(Flow flow, ProtoFlow.Builder proto) {
    for (var f : flow.flowPropertyFactors) {
      var protoFac = ProtoFlowPropertyFactor.newBuilder();
      protoFac.setConversionFactor(f.conversionFactor);
			config.dep(f.flowProperty, protoFac::setFlowProperty);
			protoFac.setIsRefFlowProperty(
				Objects.equals(f.flowProperty, flow.referenceFlowProperty));
      proto.addFlowProperties(protoFac.build());
    }
  }
}
