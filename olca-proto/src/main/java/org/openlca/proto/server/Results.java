package org.openlca.proto.server;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.proto.Messages;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

final class Results {

  private Results() {
  }

  static ImpactDescriptor findIndicator(
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

  static IndexFlow findFlow(
    SimpleResult result, Proto.Ref flowRef, Proto.Ref locationRef) {

    if (result == null || Messages.isEmpty(flowRef))
      return null;
    var index = result.flowIndex();
    if (index == null)
      return null;

    var flowId = flowRef.getId();
    var locationId = Messages.isEmpty(locationRef)
      ? null
      : Strings.nullIfEmpty(locationRef.getId());

    for (int i = 0; i < index.size(); i++) {
      var flow = index.at(i);
      if (!Strings.nullOrEqual(flow.flow.refId, flowId))
        continue;
      if (locationId == null && flow.location == null)
        return flow;
      if (locationId != null
        && flow.location != null
        && Strings.nullOrEqual(flow.location.refId, locationId))
        return flow;
    }
    return null;
  }

}
