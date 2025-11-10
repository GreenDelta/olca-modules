package org.openlca.proto.io.output;

import org.openlca.commons.Strings;
import org.openlca.core.model.ImpactMethod;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoNwFactor;
import org.openlca.proto.ProtoNwSet;
import org.openlca.proto.ProtoType;

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
    for (var impact : method.impactCategories) {
			config.dep(impact, proto::addImpactCategories);
    }
    writeNwSets(method, proto);
    return proto.build();
  }

  private void writeNwSets(
    ImpactMethod method, ProtoImpactMethod.Builder proto) {
    for (var nwSet : method.nwSets) {
      var nwProto = ProtoNwSet.newBuilder();
      nwProto.setId(Strings.notNull(nwSet.refId));
      nwProto.setName(Strings.notNull(nwSet.name));
      nwProto.setDescription(Strings.notNull(nwSet.name));
      nwProto.setWeightedScoreUnit(
        Strings.notNull(nwSet.weightedScoreUnit));
      for (var nwFactor : nwSet.factors) {
        var protoFactor = ProtoNwFactor.newBuilder();
        config.dep(nwFactor.impactCategory, protoFactor::setImpactCategory);
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
