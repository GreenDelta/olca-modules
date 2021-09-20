package org.openlca.proto.io.output;

import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.EntityType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class SocialIndicatorWriter {

  private final WriterConfig config;

  public SocialIndicatorWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.SocialIndicator write(SocialIndicator indicator) {
    var proto = Proto.SocialIndicator.newBuilder();
    if (indicator == null)
      return proto.build();
    proto.setEntityType(EntityType.SocialIndicator);
    Out.map(indicator, proto);
    Out.dep(config, indicator.category);

    if (indicator.activityQuantity != null) {
      proto.setActivityQuantity(
        Refs.refOf(indicator.activityQuantity));
      Out.dep(config, indicator.activityQuantity);
    }
    if (indicator.activityUnit != null) {
      proto.setActivityUnit(
        Refs.refOf(indicator.activityUnit));
    }
    proto.setActivityVariable(
      Strings.orEmpty(indicator.activityVariable));
    proto.setEvaluationScheme(
      Strings.orEmpty(indicator.evaluationScheme));
    proto.setUnitOfMeasurement(
      Strings.orEmpty(indicator.unitOfMeasurement));

    return proto.build();
  }
}
