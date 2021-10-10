package org.openlca.proto.io.output;

import org.openlca.core.model.ImpactMethod;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoNwFactor;
import org.openlca.proto.ProtoNwSet;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ImpactMethodWriter {

  private final WriterConfig config;

  public ImpactMethodWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoImpactMethod write(ImpactMethod method) {
    var proto = ProtoImpactMethod.newBuilder();
    if (method == null)
      return proto.build();
    proto.setType(ProtoType.ImpactMethod);
    Out.map(method, proto);
    Out.dep(config, method.category);

    for (var impact : method.impactCategories) {
      proto.addImpactCategories(Refs.refOf(impact));
      Out.dep(config, impact);
    }
    writeNwSets(method, proto);

    return proto.build();
  }

  private void writeNwSets(
    ImpactMethod method, ProtoImpactMethod.Builder proto) {
    for (var nwSet : method.nwSets) {
      var nwProto = ProtoNwSet.newBuilder();
      nwProto.setId(Strings.orEmpty(nwSet.refId));
      nwProto.setName(Strings.orEmpty(nwSet.name));
      nwProto.setDescription(Strings.orEmpty(nwSet.name));
      nwProto.setWeightedScoreUnit(
        Strings.orEmpty(nwSet.weightedScoreUnit));
      for (var nwFactor : nwSet.factors) {
        var protoFactor = ProtoNwFactor.newBuilder();
        if (nwFactor.impactCategory != null) {
          protoFactor.setImpactCategory(
            Refs.refOf(nwFactor.impactCategory));
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
