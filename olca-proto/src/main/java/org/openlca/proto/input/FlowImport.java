package org.openlca.proto.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class FlowImport implements Import<Flow> {

  private final ProtoImport imp;
  private boolean inUpdateMode;

  public FlowImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Flow> of(String id) {
    var flow = imp.get(Flow.class, id);

    // check if we are in update mode
    inUpdateMode = false;
    if (flow != null) {
      inUpdateMode = imp.shouldUpdate(flow);
      if(!inUpdateMode) {
        return ImportStatus.skipped(flow);
      }
    }

    // resolve the proto object
    var proto = imp.store.getFlow(id);
    if (proto == null)
      return flow != null
        ? ImportStatus.skipped(flow)
        : ImportStatus.error("Could not resolve Flow " + id);

    var wrap = ProtoWrap.of(proto);
    if (inUpdateMode) {
      if (imp.skipUpdate(flow, wrap))
        return ImportStatus.skipped(flow);
    }

    // map the data
    if (flow == null) {
      flow = new Flow();
      flow.refId = id;
    }
    wrap.mapTo(flow, imp);
    map(proto, flow);

    // insert or update it
    var dao = new FlowDao(imp.db);
    flow = inUpdateMode
      ? dao.update(flow)
      : dao.insert(flow);
    imp.putHandled(flow);
    return inUpdateMode
      ? ImportStatus.updated(flow)
      : ImportStatus.created(flow);
  }

  private void map(Proto.Flow proto, Flow flow) {

    flow.flowType = In.flowTypeOf(proto.getFlowType());
    flow.casNumber = proto.getCas();
    flow.synonyms = proto.getSynonyms();
    flow.synonyms = proto.getFormula();
    flow.infrastructureFlow = proto.getInfrastructureFlow();
    var locID = proto.getLocation().getId();
    if (Strings.notEmpty(locID)) {
      flow.location = new LocationImport(imp).of(locID).model();
    }

    // sync existing flow property factors if we are in update
    // mode to avoid ID changes of possibly used flow property
    // factors
    Map<String, FlowPropertyFactor> oldFactors = null;
    if (inUpdateMode) {
      oldFactors = new HashMap<>();
      for(var factor : flow.flowPropertyFactors) {
        var prop = factor.flowProperty;
        if (prop == null || prop.refId == null)
          continue;
        oldFactors.put(prop.refId, factor);
      }
      flow.flowPropertyFactors.clear();
      flow.referenceFlowProperty = null;
    }

    // flow property factors
    for (var protoFactor : proto.getFlowPropertiesList()) {
      var propID = protoFactor.getFlowProperty().getId();
      FlowPropertyFactor factor = null;
      if (oldFactors != null) {
        factor = oldFactors.get(propID);
      }
      if (factor == null) {
        factor = new FlowPropertyFactor();
      }

      if (Strings.notEmpty(propID)) {
        factor.flowProperty = new FlowPropertyImport(imp)
          .of(propID)
          .model();
      }
      factor.conversionFactor = protoFactor.getConversionFactor();
      if (protoFactor.getReferenceFlowProperty()) {
        flow.referenceFlowProperty = factor.flowProperty;
      }
      flow.flowPropertyFactors.add(factor);
    }
  }
}
