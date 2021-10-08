package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.util.Strings;

class SocialIndicatorImport implements Import<SocialIndicator> {

  private final ProtoImport imp;

  SocialIndicatorImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<SocialIndicator> of(String id) {
    var indicator = imp.get(SocialIndicator.class, id);

    // check if we are in update mode
    var update = false;
    if (indicator != null) {
      update = imp.shouldUpdate(indicator);
      if(!update) {
        return ImportStatus.skipped(indicator);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getSocialIndicator(id);
    if (proto == null)
      return indicator != null
        ? ImportStatus.skipped(indicator)
        : ImportStatus.error("Could not resolve SocialIndicator " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(indicator, wrap))
        return ImportStatus.skipped(indicator);
    }

    // map the data
    if (indicator == null) {
      indicator = new SocialIndicator();
      indicator.refId = id;
    }
    wrap.mapTo(indicator, imp);
    map(proto, indicator);

    // insert or update it
    var dao = new SocialIndicatorDao(imp.db);
    indicator = update
      ? dao.update(indicator)
      : dao.insert(indicator);
    imp.putHandled(indicator);
    return update
      ? ImportStatus.updated(indicator)
      : ImportStatus.created(indicator);
  }

  private void map(ProtoSocialIndicator proto, SocialIndicator indicator) {
    indicator.evaluationScheme = proto.getEvaluationScheme();
    indicator.unitOfMeasurement = proto.getUnitOfMeasurement();
    indicator.activityVariable = proto.getActivityVariable();

    // quantity (flow property)
    var quantityID = proto.getActivityQuantity().getId();
    if (Strings.notEmpty(quantityID)) {
      indicator.activityQuantity = new FlowPropertyImport(imp)
        .of(quantityID)
        .model();
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
