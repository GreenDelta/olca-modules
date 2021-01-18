package org.openlca.proto.output;

import org.openlca.core.model.ImpactMethod;
import org.openlca.proto.generated.Proto;
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
    Out.map(method, proto);
    Out.dep(config, method.category);

    for (var impact : method.impactCategories) {
      proto.addImpactCategories(Out.refOf(impact));
      Out.dep(config, impact);
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
            Out.refOf(nwFactor.impactCategory));
          Out.dep(config, nwFactor.impactCategory);
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
