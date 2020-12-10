package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
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

    // root entity fields
    proto.setType("Flow");
    proto.setId(Strings.orEmpty(flow.refId));
    proto.setName(Strings.orEmpty(flow.name));
    proto.setDescription(Strings.orEmpty(flow.description));
    proto.setVersion(Version.asString(flow.version));
    if (flow.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(flow.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(flow.tags)) {
      Arrays.stream(flow.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (flow.category != null) {
      proto.setCategory(Out.refOf(flow.category, config));
    }

    // model specific fields
    proto.setCas(Strings.orEmpty(flow.casNumber));
    proto.setFormula(Strings.orEmpty(flow.formula));
    proto.setInfrastructureFlow(flow.infrastructureFlow);
    proto.setSynonyms(Strings.orEmpty(flow.synonyms));
    proto.setFlowType(Out.flowTypeOf(flow.flowType));
    if (flow.location != null) {
      proto.setLocation(Out.refOf(flow.location, config));
    }
    writeFlowProperties(flow, proto);

    return proto.build();
  }

  private void writeFlowProperties(Flow flow, Proto.Flow.Builder proto) {
    for (var f : flow.flowPropertyFactors) {
      var protoF = Proto.FlowPropertyFactor.newBuilder();
      protoF.setConversionFactor(f.conversionFactor);
      if (f.flowProperty != null) {
        protoF.setFlowProperty(Out.refOf(f.flowProperty, config));
        protoF.setReferenceFlowProperty(
          Objects.equals(f.flowProperty, flow.referenceFlowProperty));
      }
      proto.addFlowProperties(protoF.build());
    }
  }
}
