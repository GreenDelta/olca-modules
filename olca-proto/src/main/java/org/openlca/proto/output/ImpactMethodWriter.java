package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ImpactMethodWriter {

  private final WriterConfig config;

  public ImpactMethodWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.ImpactMethod write(ImpactMethod method) {
    var proto = Proto.ImpactMethod.newBuilder();
    if (method == null)
      return proto.build();

    // root entity fields
    proto.setType("ImpactMethod");
    proto.setId(Strings.orEmpty(method.refId));
    proto.setName(Strings.orEmpty(method.name));
    proto.setDescription(Strings.orEmpty(method.description));
    proto.setVersion(Version.asString(method.version));
    if (method.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(method.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(method.tags)) {
      Arrays.stream(method.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (method.category != null) {
      proto.setCategory(Out.refOf(method.category, config));
    }

    // model specific fields
    for (var impact : method.impactCategories) {
      proto.addImpactCategories(Out.impactRefOf(impact, config));
    }
    writeNwSets(method, proto);

    return proto.build();
  }

  private void writeNwSets(
    ImpactMethod method, Proto.ImpactMethod.Builder proto) {
    for (var nwSet : method.nwSets) {
      var nwProto = Proto.NwSet.newBuilder();
      nwProto.setId(Strings.orEmpty(nwSet.refId));
      nwProto.setName(Strings.orEmpty(nwSet.name));
      nwProto.setDescription(Strings.orEmpty(nwSet.name));
      nwProto.setWeightedScoreUnit(
        Strings.orEmpty(nwSet.weightedScoreUnit));
      for (var nwFactor : nwSet.factors) {
        var protoFactor = Proto.NwFactor.newBuilder();
        if (nwFactor.impactCategory != null) {
          protoFactor.setImpactCategory(
            Out.impactRefOf(nwFactor.impactCategory, config));
        }
        if (nwFactor.normalisationFactor != null) {
          protoFactor.setNormalisationFactor(
            nwFactor.normalisationFactor);
        }
        if (nwFactor.weightingFactor != null) {
          protoFactor.setWeightingFactor(
            nwFactor.weightingFactor);
        }
        nwProto.addFactors(protoFactor.build());
      }
      proto.addNwSets(nwProto.build());
    }
  }
}
