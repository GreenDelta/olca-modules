package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
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

    // root entity fields
    proto.setType("UnitGroup");
    proto.setId(Strings.orEmpty(group.refId));
    proto.setName(Strings.orEmpty(group.name));
    proto.setDescription(Strings.orEmpty(group.description));
    proto.setVersion(Version.asString(group.version));
    if (group.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(group.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(group.tags)) {
      Arrays.stream(group.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (group.category != null) {
      proto.setCategory(Out.refOf(group.category, config));
    }

    // model specific fields
    if (group.defaultFlowProperty != null) {
      proto.setDefaultFlowProperty(
        Out.refOf(group.defaultFlowProperty, config));
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
