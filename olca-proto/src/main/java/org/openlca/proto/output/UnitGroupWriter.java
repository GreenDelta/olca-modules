package org.openlca.proto.output;

import java.util.Arrays;
import java.util.Objects;

import org.openlca.core.model.UnitGroup;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class UnitGroupWriter {

  private final WriterConfig config;

  public UnitGroupWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.UnitGroup write(UnitGroup group) {
    var proto = Proto.UnitGroup.newBuilder();
    if (group == null)
      return proto.build();
    Out.map(group, proto);
    Out.dep(config, group.category);

    if (group.defaultFlowProperty != null) {
      proto.setDefaultFlowProperty(
        Out.refOf(group.defaultFlowProperty));
      Out.dep(config, group.defaultFlowProperty);
    }
    writeUnits(group, proto);
    return proto.build();
  }

  private void writeUnits(
    UnitGroup group, Proto.UnitGroup.Builder proto) {
    for (var unit : group.units) {
      var protoUnit = Proto.Unit.newBuilder();
      protoUnit.setId(Strings.orEmpty(unit.refId));
      protoUnit.setName(Strings.orEmpty(unit.name));
      protoUnit.setDescription(Strings.orEmpty(unit.description));
      protoUnit.setConversionFactor(unit.conversionFactor);
      if (unit.synonyms != null) {
        Arrays.stream(unit.synonyms.split(";"))
          .map(String::trim)
          .filter(Strings::notEmpty)
          .forEach(protoUnit::addSynonyms);
      }
      protoUnit.setReferenceUnit(
        Objects.equals(unit, group.referenceUnit));
      proto.addUnits(protoUnit.build());
    }
  }
}
