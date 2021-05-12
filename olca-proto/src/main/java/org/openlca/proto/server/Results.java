package org.openlca.proto.server;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.proto.Messages;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.results.ResultsProto;
import org.openlca.proto.output.Refs;
import org.openlca.util.Strings;

final class Results {

  private Results() {
  }

  static ImpactDescriptor findImpact(
    SimpleResult result, Proto.Ref indicatorRef) {

    if (result == null || Messages.isEmpty(indicatorRef))
      return null;
    var index = result.impactIndex();
    if (index == null)
      return null;

    var indicatorId = indicatorRef.getId();
    for (int i = 0; i < index.size(); i++) {
      var indicator = index.at(i);
      if (Strings.nullOrEqual(indicatorId, indicator.refId)) {
        return indicator;
      }
    }
    return null;
  }

  static EnviFlow findFlow(
    SimpleResult result, ResultsProto.EnviFlow proto) {

    if (result == null || Messages.isEmpty(proto))
      return null;
    var index = result.enviIndex();
    if (index == null)
      return null;

    var flowId = proto.getFlow().getId();
    var locationId = Messages.isEmpty(proto.getLocation())
      ? null
      : Strings.nullIfEmpty(proto.getLocation().getId());

    for (var flow : index) {
      if (!Strings.nullOrEqual(flow.flow().refId, flowId))
        continue;
      if (locationId == null && flow.location() == null)
        return flow;
      if (locationId != null
        && flow.location() != null
        && Strings.nullOrEqual(flow.location().refId, locationId))
        return flow;
    }
    return null;
  }

  static TechFlow findProduct(
    SimpleResult result, ResultsProto.TechFlow proto) {
    if (result == null || Messages.isEmpty(proto))
      return null;
    var processId = proto.getProcess().getId();
    var flowId = proto.hasProduct()
      ? proto.getProduct().getId()
      : proto.getWaste().getId();
    for (var p : result.techIndex()) {
      if (p.process() == null || p.flow() == null)
        continue;
      if (Strings.nullOrEqual(p.process().refId, processId)
        && Strings.nullOrEqual(p.flow().refId, flowId))
        return p;
    }
    return null;
  }

  static ResultsProto.EnviFlow toProto(
    EnviFlow flow, Refs.RefData refData) {

    var proto = ResultsProto.EnviFlow.newBuilder();
    if (flow == null)
      return proto.build();
    proto.setFlow(Refs.refOf(flow.flow(), refData));
    proto.setIsInput(flow.isInput());
    if (flow.location() != null) {
      proto.setLocation(Refs.refOf(flow.location(), refData));
    }
    return proto.build();
  }

  static ResultsProto.ResultValue toProtoResult(
    EnviFlow flow, Refs.RefData refData, double value) {
    return ResultsProto.ResultValue.newBuilder()
      .setEnviFlow(toProto(flow, refData))
      .setValue(value)
      .build();
  }

  static ResultsProto.TechFlow toProto(
    TechFlow product, Refs.RefData refData) {

    var proto = ResultsProto.TechFlow.newBuilder();
    if (product == null)
      return proto.build();
    if (product.process() != null) {
      proto.setProcess(Refs.refOf(product.process(), refData));
    }
    if (product.flow() != null) {
      if (product.isWaste()) {
        proto.setWaste(Refs.refOf(product.flow(), refData));
      } else {
        proto.setProduct(Refs.refOf(product.flow(), refData));
      }
    }
    return proto.build();
  }

}
