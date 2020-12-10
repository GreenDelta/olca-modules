package org.openlca.proto.input;

import java.util.Objects;

import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class SocialIndicatorImport {

  private final ProtoImport imp;

  public SocialIndicatorImport(ProtoImport imp) {
    this.imp = imp;
  }

  public SocialIndicator of(String id) {
    if (id == null)
      return null;
    var indicator = imp.get(SocialIndicator.class, id);

    // check if we are in update mode
    var update = false;
    if (indicator != null) {
      update = imp.shouldUpdate(indicator);
      if(!update) {
        return indicator;
      }
    }

    // check the proto object
    var proto = imp.store.getSocialIndicator(id);
    if (proto == null)
      return indicator;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(indicator, wrap))
        return indicator;
    }

    // map the data
    if (indicator == null) {
      indicator = new SocialIndicator();
      indicator.refId = id;
    }
    wrap.mapTo(indicator, imp);
    map(proto, indicator);

    // insert it
    var dao = new SocialIndicatorDao(imp.db);
    indicator = update
      ? dao.update(indicator)
      : dao.insert(indicator);
    imp.putHandled(indicator);
    return indicator;
  }

  private void map(Proto.SocialIndicator proto, SocialIndicator indicator) {
    indicator.evaluationScheme = proto.getEvaluationScheme();
    indicator.unitOfMeasurement = proto.getUnitOfMeasurement();
    indicator.activityVariable = proto.getActivityVariable();

    // quantity (flow property)
    var quantityID = proto.getActivityQuantity().getId();
    if (Strings.notEmpty(quantityID)) {
      indicator.activityQuantity = new FlowPropertyImport(imp)
        .of(quantityID);
    }
    if (indicator.activityQuantity == null)
      return;

    // unit
    var unitID = proto.getActivityUnit().getId();
    if (Strings.nullOrEmpty(unitID))
      return;
    var group = indicator.activityQuantity.unitGroup;
    if (group == null)
      return;
    indicator.activityUnit = group.units.stream()
      .filter(u -> Objects.equals(u.refId, unitID))
      .findAny()
      .orElse(null);
  }
}
