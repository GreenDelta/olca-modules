package org.openlca.proto.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class UnitGroupImport {

  private final ProtoImport imp;
  private boolean inUpdateMode;

  public UnitGroupImport(ProtoImport imp) {
    this.imp = imp;
  }

  public UnitGroup of(String id) {
    if (id == null)
      return null;
    var group = imp.get(UnitGroup.class, id);

    // check if we are in update mode
    inUpdateMode = false;
    if (group != null) {
      inUpdateMode = imp.shouldUpdate(group);
      if(!inUpdateMode) {
        return group;
      }
    }

    // check the proto object
    var proto = imp.store.getUnitGroup(id);
    if (proto == null)
      return group;
    var wrap = ProtoWrap.of(proto);
    if (inUpdateMode) {
      if (imp.skipUpdate(group, wrap))
        return group;
    }

    // map the data
    if (group == null) {
      group = new UnitGroup();
      group.refId = id;
    }
    wrap.mapTo(group, imp);
    map(proto, group);

    // insert it
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
        .of(propID);
      group = dao.update(group);
    }

    return group;
  }

  private void map(Proto.UnitGroup proto, UnitGroup group) {

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

  private void mapUnit(Proto.Unit proto, Unit unit) {
    unit.refId = proto.getId();
    unit.name = proto.getName();
    unit.description = proto.getDescription();
    unit.conversionFactor = proto.getConversionFactor();
    unit.synonyms = proto.getSynonymsList().stream()
      .reduce((syn, acc) -> syn + ";" + acc)
      .orElse(null);
  }
}

