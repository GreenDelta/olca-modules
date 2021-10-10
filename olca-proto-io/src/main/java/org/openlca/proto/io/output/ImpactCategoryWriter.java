package org.openlca.proto.io.output;

import org.openlca.core.model.ImpactCategory;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactFactor;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ImpactCategoryWriter {

  private final WriterConfig config;

  public ImpactCategoryWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoImpactCategory write(ImpactCategory impact) {
    var proto = ProtoImpactCategory.newBuilder();
    if (impact == null)
      return proto.build();
    proto.setType(ProtoType.ImpactCategory);
    Out.map(impact, proto);
    Out.dep(config, impact.category);

    proto.setReferenceUnitName(
      Strings.orEmpty(impact.referenceUnit));
    writeFactors(impact, proto);
    var paramWriter = new ParameterWriter(config);
    for (var param : impact.parameters) {
      proto.addParameters(paramWriter.write(param));
    }
    return proto.build();
  }

  private void writeFactors(
    ImpactCategory impact, ProtoImpactCategory.Builder proto) {
    for (var factor : impact.impactFactors) {
      var protoFac = ProtoImpactFactor.newBuilder();

      if (factor.flow != null) {
        protoFac.setFlow(Refs.refOf(factor.flow));
        Out.dep(config, factor.flow);
      }

      var prop = factor.flowPropertyFactor;
      if (prop != null && prop.flowProperty != null) {
        protoFac.setFlowProperty(Refs.refOf(prop.flowProperty));
      }

      protoFac.setFormula(Strings.orEmpty(factor.formula));

      if (factor.location != null) {
        protoFac.setLocation(Refs.refOf(factor.location));
        Out.dep(config, factor.location);
      }

      if (factor.uncertainty != null) {
        protoFac.setUncertainty(Out.uncertaintyOf(factor.uncertainty));
      }

      if (factor.unit != null) {
        protoFac.setUnit(Refs.refOf(factor.unit));
      }

      protoFac.setValue(factor.value);
      proto.addImpactFactors(protoFac.build());
    }
  }
}
