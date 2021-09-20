package org.openlca.proto.io.output;

import org.openlca.core.model.DQSystem;
import org.openlca.proto.EntityType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class DQSystemWriter {

  private final WriterConfig config;

  public DQSystemWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.DQSystem write(DQSystem dqSystem) {
    var proto = Proto.DQSystem.newBuilder();
    if (dqSystem == null)
      return proto.build();
    proto.setEntityType(EntityType.DQSystem);
    Out.map(dqSystem, proto);
    Out.dep(config, dqSystem.category);

    proto.setHasUncertainties(dqSystem.hasUncertainties);
    if (dqSystem.source != null) {
      proto.setSource(Refs.refOf(dqSystem.source));
      Out.dep(config, dqSystem.source);
    }
    writeIndicators(dqSystem, proto);
    return proto.build();
  }

  private void writeIndicators(
    DQSystem dqSystem, Proto.DQSystem.Builder proto) {
    for (var indicator : dqSystem.indicators) {
      var protoInd = Proto.DQIndicator.newBuilder();
      protoInd.setName(Strings.orEmpty(indicator.name));
      protoInd.setPosition(indicator.position);
      for (var score: indicator.scores) {
        var protoScore = Proto.DQScore.newBuilder();
        protoScore.setDescription(Strings.orEmpty(score.description));
        protoScore.setLabel(Strings.orEmpty(score.label));
        protoScore.setPosition(score.position);
        protoScore.setUncertainty(score.uncertainty);
        protoInd.addScores(protoScore.build());
      }
      proto.addIndicators(protoInd.build());
    }
  }
}
