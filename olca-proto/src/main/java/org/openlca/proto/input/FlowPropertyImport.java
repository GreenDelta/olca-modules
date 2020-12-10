package org.openlca.proto.input;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class FlowPropertyImport {

  private final ProtoImport imp;

  public FlowPropertyImport(ProtoImport imp) {
    this.imp = imp;
  }

  public FlowProperty of(String id) {
    if (id == null)
      return null;
    var flowProperty = imp.get(FlowProperty.class, id);

    // check if we are in update mode
    var update = false;
    if (flowProperty != null) {
      update = imp.shouldUpdate(flowProperty);
      if(!update) {
        return flowProperty;
      }
    }

    // check the proto object
    var proto = imp.store.getFlowProperty(id);
    if (proto == null)
      return flowProperty;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(flowProperty, wrap))
        return flowProperty;
    }

    // map the data
    if (flowProperty == null) {
      flowProperty = new FlowProperty();
      flowProperty.refId = id;
    }
    wrap.mapTo(flowProperty, imp);
    map(proto, flowProperty);

    // insert it
    var dao = new FlowPropertyDao(imp.db);
    flowProperty = update
      ? dao.update(flowProperty)
      : dao.insert(flowProperty);
    imp.putHandled(flowProperty);
    return flowProperty;
  }

  private void map(Proto.FlowProperty proto, FlowProperty flowProperty) {
    var type = proto.getFlowPropertyType();
    flowProperty.flowPropertyType = type == Proto.FlowPropertyType.ECONOMIC_QUANTITY
      ? FlowPropertyType.ECONOMIC
      : FlowPropertyType.PHYSICAL;
    var unitGroupID = proto.getUnitGroup().getId();
    if (Strings.notEmpty(unitGroupID)) {
      flowProperty.unitGroup = new UnitGroupImport(imp)
        .of(unitGroupID);
    }
  }
}

