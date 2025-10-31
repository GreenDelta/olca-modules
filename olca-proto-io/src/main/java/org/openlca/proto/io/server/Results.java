package org.openlca.proto.io.server;

import java.util.Objects;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.grpc.ProtoEnviFlow;
import org.openlca.proto.grpc.ProtoTechFlow;
import org.openlca.proto.grpc.ResultValue;
import org.openlca.proto.io.Messages;
import org.openlca.proto.io.output.Refs;
import org.openlca.util.Strings;

final class Results {

  private Results() {
  }

  static ImpactDescriptor findImpact(LcaResult result, ProtoRef ref) {

    if (result == null || Messages.isEmpty(ref))
      return null;
    var index = result.impactIndex();
    if (index == null)
      return null;

    var indicatorId = ref.getId();
    for (int i = 0; i < index.size(); i++) {
      var indicator = index.at(i);
			if (Objects.equals(indicatorId, indicator.refId)) {
        return indicator;
      }
    }
    return null;
  }

  static EnviFlow findFlow(LcaResult result, ProtoEnviFlow proto) {

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
			if (!Objects.equals(flow.flow().refId, flowId))
        continue;
      if (locationId == null && flow.location() == null)
        return flow;
      if (locationId != null
        && flow.location() != null
				&& Objects.equals(flow.location().refId, locationId))
        return flow;
    }
    return null;
  }

  static TechFlow findProduct(LcaResult result, ProtoTechFlow proto) {
    if (result == null || Messages.isEmpty(proto))
      return null;
    var processId = proto.getProvider().getId();
    var flowId = proto.hasProduct()
      ? proto.getProduct().getId()
      : proto.getWaste().getId();
    for (var p : result.techIndex()) {
      if (p.provider() == null || p.flow() == null)
        continue;
			if (Objects.equals(p.provider().refId, processId)
				&& Objects.equals(p.flow().refId, flowId))
        return p;
    }
    return null;
  }

  static ProtoEnviFlow toProto(EnviFlow flow, Refs.RefData refData) {

    var proto = ProtoEnviFlow.newBuilder();
    if (flow == null)
      return proto.build();
    proto.setFlow(Refs.refOf(flow.flow(), refData));
    proto.setIsInput(flow.isInput());
    if (flow.location() != null) {
      proto.setLocation(Refs.refOf(flow.location(), refData));
    }
    return proto.build();
  }

  static ResultValue toProtoResult(
    EnviFlow flow, Refs.RefData refData, double value) {
    return ResultValue.newBuilder()
      .setEnviFlow(toProto(flow, refData))
      .setValue(value)
      .build();
  }

  static ProtoTechFlow toProto(TechFlow product, Refs.RefData refData) {

    var proto = ProtoTechFlow.newBuilder();
    if (product == null)
      return proto.build();
    if (product.provider() != null) {
      proto.setProvider(Refs.refOf(product.provider(), refData));
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
