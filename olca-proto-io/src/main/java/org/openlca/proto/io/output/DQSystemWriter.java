package org.openlca.proto.io.output;

import org.openlca.core.model.DQSystem;
import org.openlca.proto.ProtoDQIndicator;
import org.openlca.proto.ProtoDQScore;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class DQSystemWriter {

  private final WriterConfig config;

  public DQSystemWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoDQSystem write(DQSystem dqSystem) {
    var proto = ProtoDQSystem.newBuilder();
    if (dqSystem == null)
      return proto.build();
    proto.setType(ProtoType.DQSystem);
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
    DQSystem dqSystem, ProtoDQSystem.Builder proto) {
    for (var indicator : dqSystem.indicators) {
      var protoInd = ProtoDQIndicator.newBuilder();
      protoInd.setName(Strings.orEmpty(indicator.name));
      protoInd.setPosition(indicator.position);
      for (var score : indicator.scores) {
        var protoScore = ProtoDQScore.newBuilder();
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
