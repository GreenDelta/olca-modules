package org.openlca.proto.output;

import org.openlca.core.model.ImpactCategory;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class ImpactCategoryWriter {

  private final WriterConfig config;

  public ImpactCategoryWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.ImpactCategory write(ImpactCategory impact) {
    var proto = Proto.ImpactCategory.newBuilder();
    if (impact == null)
      return proto.build();
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
    ImpactCategory impact, Proto.ImpactCategory.Builder proto) {
    for (var factor : impact.impactFactors) {
      var protoFac = Proto.ImpactFactor.newBuilder();

      if (factor.flow != null) {
        protoFac.setFlow(Out.flowRefOf(factor.flow));
        Out.dep(config, factor.flow);
      }

      var prop = factor.flowPropertyFactor;
      if (prop != null && prop.flowProperty != null) {
        protoFac.setFlowProperty(Out.refOf(prop.flowProperty));
      }

      protoFac.setFormula(Strings.orEmpty(factor.formula));

      if (factor.location != null) {
        protoFac.setLocation(Out.refOf(factor.location));
        Out.dep(config, factor.location);
      }

      if (factor.uncertainty != null) {
        protoFac.setUncertainty(Out.uncertaintyOf(factor.uncertainty));
      }

      if (factor.unit != null) {
        protoFac.setUnit(Out.refOf(factor.unit));
      }

      protoFac.setValue(factor.value);
      proto.addImpactFactors(protoFac.build());
    }
  }
}
