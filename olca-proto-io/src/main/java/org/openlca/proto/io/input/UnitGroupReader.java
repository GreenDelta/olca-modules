
package org.openlca.proto.io.input;

import java.util.HashMap;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoUnitGroup;
import org.openlca.util.Strings;

public record UnitGroupReader(EntityResolver resolver)
	implements EntityReader<UnitGroup, ProtoUnitGroup> {

	@Override
	public UnitGroup read(ProtoUnitGroup proto) {
		var group = new UnitGroup();
		update(group, proto);
		return group;
	}

	@Override
	public void update(UnitGroup group, ProtoUnitGroup proto) {
		Util.mapBase(group, ProtoWrap.of(proto), resolver);
		group.defaultFlowProperty = Util.getFlowProperty(
			resolver, proto.getDefaultFlowProperty());
		mapUnits(group, proto);
	}

	private void mapUnits(UnitGroup group, ProtoUnitGroup proto) {

		// sync. with existing units if we are in update mode
		var oldUnits = new HashMap<String, Unit>();
		for (var oldUnit : group.units) {
			oldUnits.put(oldUnit.name, oldUnit);
		}
		group.units.clear();

		for (int i = 0; i < proto.getUnitsCount(); i++) {
			var protoUnit = proto.getUnits(i);
			if (Strings.nullOrEmpty(protoUnit.getName()))
				continue;
			var unit = oldUnits.computeIfAbsent(
				protoUnit.getName(), n -> new Unit());
			unit.name = protoUnit.getName();
			unit.refId = protoUnit.getId();
			unit.description = protoUnit.getDescription();
			unit.conversionFactor = protoUnit.getConversionFactor();
			var protoSyns = protoUnit.getSynonymsList();
			if (!protoSyns.isEmpty()) {
				unit.synonyms = String.join(";", protoSyns);
			}
			if (protoUnit.getIsRefUnit()) {
				group.referenceUnit = unit;
			}
			group.units.add(unit);
		}
	}
}
