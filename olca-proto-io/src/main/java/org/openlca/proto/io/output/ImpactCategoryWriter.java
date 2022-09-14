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
    proto.setRefUnit(Strings.orEmpty(impact.referenceUnit));
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
			config.dep(factor.flow, protoFac::setFlow);
			config.dep(factor.unit, protoFac::setUnit);
      var prop = factor.flowPropertyFactor;
      if (prop != null) {
				config.dep(prop.flowProperty, protoFac::setFlowProperty);
      }
      protoFac.setFormula(Strings.orEmpty(factor.formula));
			config.dep(factor.location, protoFac::setLocation);
      if (factor.uncertainty != null) {
        protoFac.setUncertainty(Out.uncertaintyOf(factor.uncertainty));
      }
      protoFac.setValue(factor.value);
      proto.addImpactFactors(protoFac.build());
    }
  }
}
