package org.openlca.proto.input;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class FlowPropertyImport implements Import<FlowProperty> {

  private final ProtoImport imp;

  public FlowPropertyImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<FlowProperty> of(String id) {
    var flowProperty = imp.get(FlowProperty.class, id);

    // check if we are in update mode
    var update = false;
    if (flowProperty != null) {
      update = imp.shouldUpdate(flowProperty);
      if(!update) {
        return ImportStatus.skipped(flowProperty);
      }
    }

    // resolve the proto object
    var proto = imp.store.getFlowProperty(id);
    if (proto == null)
      return flowProperty != null
        ? ImportStatus.skipped(flowProperty)
        : ImportStatus.error("Could not resolve FlowProperty " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(flowProperty, wrap))
        return ImportStatus.skipped(flowProperty);
    }

    // map the data
    if (flowProperty == null) {
      flowProperty = new FlowProperty();
      flowProperty.refId = id;
    }
    wrap.mapTo(flowProperty, imp);
    map(proto, flowProperty);

    // insert or update it
    var dao = new FlowPropertyDao(imp.db);
    flowProperty = update
      ? dao.update(flowProperty)
      : dao.insert(flowProperty);
    imp.putHandled(flowProperty);
    return update
      ? ImportStatus.updated(flowProperty)
      : ImportStatus.created(flowProperty);
  }

  private void map(Proto.FlowProperty proto, FlowProperty flowProperty) {
    var type = proto.getFlowPropertyType();
    flowProperty.flowPropertyType = type == Proto.FlowPropertyType.ECONOMIC_QUANTITY
      ? FlowPropertyType.ECONOMIC
      : FlowPropertyType.PHYSICAL;
    var unitGroupID = proto.getUnitGroup().getId();
    if (Strings.notEmpty(unitGroupID)) {
      flowProperty.unitGroup = new UnitGroupImport(imp)
        .of(unitGroupID)
        .model();
    }
  }
}

