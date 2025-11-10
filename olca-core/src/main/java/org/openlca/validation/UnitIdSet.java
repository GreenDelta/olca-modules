package org.openlca.validation;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

class UnitIdSet {

	private final TLongObjectHashMap<TLongHashSet> flowFactors;
	private final TLongLongHashMap factorProps;
	private final TLongObjectHashMap<TLongHashSet> propUnits;

	private UnitIdSet(IDatabase db) {
		flowFactors = new TLongObjectHashMap<>();
		factorProps = new TLongLongHashMap();
		propUnits = new TLongObjectHashMap<>();

		var sql = NativeSql.on(db);

		var factorQuery = """
				select id, f_flow, f_flow_property
				  from tbl_flow_property_factors""";
		sql.query(factorQuery, r -> {
			var factorId = r.getLong(1);
			var flowId = r.getLong(2);
			var propId = r.getLong(3);
			setOf(flowId, flowFactors).add(factorId);
			factorProps.put(factorId, propId);
			return true;
		});

		var unitQuery = """
				select prop.id, unit.id
				  from tbl_flow_properties prop
				  inner join tbl_units unit
				  on prop.f_unit_group = unit.f_unit_group""";
		sql.query(unitQuery, r -> {
			long propId = r.getLong(1);
			long unitId = r.getLong(2);
			setOf(propId, propUnits).add(unitId);
			return true;
		});

	}

	private TLongHashSet setOf(long key, TLongObjectHashMap<TLongHashSet> map) {
		var existing = map.get(key);
		if (existing != null)
			return existing;
		var set = new TLongHashSet();
		map.put(key, set);
		return set;
	}

	static UnitIdSet create(IDatabase db) {
		return new UnitIdSet(db);
	}

	boolean isFlowUnit(long flowId, long factorId, long unitId) {
		var factors = flowFactors.get(flowId);
		return factors != null
				&& factors.contains(factorId)
				&& isFactorUnit(factorId, unitId);
	}

	boolean isFactorUnit(long factorId, long unitId) {
		var propId = factorProps.get(factorId);
		return propId != 0 && isPropertyUnit(propId, unitId);
	}

	boolean isPropertyUnit(long propId, long unitId) {
		var units = propUnits.get(propId);
		return units != null && units.contains(unitId);
	}

}
