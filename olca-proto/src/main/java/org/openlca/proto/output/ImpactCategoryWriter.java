package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
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

    // root entity fields
    proto.setType("ImpactCategory");
    proto.setId(Strings.orEmpty(impact.refId));
    proto.setName(Strings.orEmpty(impact.name));
    proto.setDescription(Strings.orEmpty(impact.description));
    proto.setVersion(Version.asString(impact.version));
    if (impact.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(impact.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(impact.tags)) {
      Arrays.stream(impact.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (impact.category != null) {
      proto.setCategory(Out.refOf(impact.category, config));
    }

    // model specific fields
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
        protoFac.setFlow(Out.flowRefOf(factor.flow, config));
      }

      var prop = factor.flowPropertyFactor;
      if (prop != null && prop.flowProperty != null) {
        protoFac.setFlowProperty(Out.refOf(prop.flowProperty));
      }

      protoFac.setFormula(Strings.orEmpty(factor.formula));

      if (factor.location != null) {
        protoFac.setLocation(Out.refOf(factor.location, config));
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
