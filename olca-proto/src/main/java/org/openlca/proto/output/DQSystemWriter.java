package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Version;
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

    // root entity fields
    proto.setType("DQSystem");
    proto.setId(Strings.orEmpty(dqSystem.refId));
    proto.setName(Strings.orEmpty(dqSystem.name));
    proto.setDescription(Strings.orEmpty(dqSystem.description));
    proto.setVersion(Version.asString(dqSystem.version));
    if (dqSystem.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(dqSystem.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(dqSystem.tags)) {
      Arrays.stream(dqSystem.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (dqSystem.category != null) {
      proto.setCategory(Out.refOf(dqSystem.category, config));
    }

    // model specific fields
    proto.setHasUncertainties(dqSystem.hasUncertainties);
    if (dqSystem.source != null) {
      proto.setSource(Out.refOf(dqSystem.source, config));
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
