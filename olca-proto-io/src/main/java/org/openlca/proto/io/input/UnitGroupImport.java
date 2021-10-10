package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoUnit;
import org.openlca.proto.ProtoUnitGroup;
import org.openlca.util.Strings;

class UnitGroupImport implements Import<UnitGroup> {

  private final ProtoImport imp;

  UnitGroupImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<UnitGroup> of(String id) {
    var group = imp.get(UnitGroup.class, id);

    // check if we are in update mode
    var inUpdateMode = false;
    if (group != null) {
      inUpdateMode = imp.shouldUpdate(group);
      if(!inUpdateMode) {
        return ImportStatus.skipped(group);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getUnitGroup(id);
    if (proto == null)
      return group != null
        ? ImportStatus.skipped(group)
        : ImportStatus.error("Could not resolve UnitGroup " +id);

    var wrap = ProtoWrap.of(proto);
    if (inUpdateMode) {
      if (imp.skipUpdate(group, wrap))
        return ImportStatus.skipped(group);
    }

    // map the data
    if (group == null) {
      group = new UnitGroup();
      group.refId = id;
    }
    wrap.mapTo(group, imp);
    map(proto, group, inUpdateMode);

    // insert or update it
    var dao = new UnitGroupDao(imp.db);
    group = inUpdateMode
      ? dao.update(group)
      : dao.insert(group);
    imp.putHandled(group);

    // set a possible default flow property after
    // the unit group was saved to avoid endless
    // import cycles
    var propID = proto.getDefaultFlowProperty().getId();
    if (Strings.notEmpty(propID)) {
      group.defaultFlowProperty = new FlowPropertyImport(imp)
        .of(propID)
        .model();
      group = dao.update(group);
    }

    return inUpdateMode
      ? ImportStatus.updated(group)
      : ImportStatus.created(group);
  }

  private void map(
    ProtoUnitGroup proto, UnitGroup group, boolean inUpdateMode) {

    // sync units (keep the IDs) if we are in update mode
    // this is important because these units may are used
    // in exchanges etc. and we do not want to break these
    // pointers when updating an unit
    Map<String, Unit> oldUnits = null;
    if (inUpdateMode) {
      oldUnits = new HashMap<>();
      for (var unit : group.units) {
        oldUnits.put(unit.name, unit);
      }
      group.units.clear();
      group.referenceUnit = null;
    }

    for (var protoUnit : proto.getUnitsList()) {
      Unit unit = null;
      if (oldUnits != null) {
        unit = oldUnits.get(protoUnit.getName());
      }
      if (unit == null) {
        unit = new Unit();
      }
      mapUnit(protoUnit, unit);
      if (protoUnit.getReferenceUnit()) {
        group.referenceUnit = unit;
      }
      group.units.add(unit);
    }
  }

  private void mapUnit(ProtoUnit proto, Unit unit) {
    unit.refId = proto.getId();
    unit.name = proto.getName();
    unit.description = proto.getDescription();
    unit.conversionFactor = proto.getConversionFactor();
    unit.synonyms = proto.getSynonymsList().stream()
      .reduce((syn, acc) -> syn + ";" + acc)
      .orElse(null);
  }
}

