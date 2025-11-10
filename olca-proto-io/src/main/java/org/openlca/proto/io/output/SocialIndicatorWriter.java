package org.openlca.proto.io.output;

import org.openlca.commons.Strings;
import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoType;

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
		config.dep(indicator.activityQuantity, proto::setActivityQuantity);
    config.dep(indicator.activityUnit, proto::setActivityUnit);
    proto.setActivityVariable(Strings.notNull(indicator.activityVariable));
    proto.setEvaluationScheme(Strings.notNull(indicator.evaluationScheme));
    proto.setUnitOfMeasurement(Strings.notNull(indicator.unitOfMeasurement));
    return proto.build();
  }
}
