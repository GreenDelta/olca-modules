package org.openlca.proto.io.output;

import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class SocialIndicatorWriter {

  private final WriterConfig config;

  public SocialIndicatorWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoSocialIndicator write(SocialIndicator indicator) {
    var proto = ProtoSocialIndicator.newBuilder();
    if (indicator == null)
      return proto.build();
    proto.setType(ProtoType.SocialIndicator);
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
