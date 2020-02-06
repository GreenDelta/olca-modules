package org.openlca.core.matrix;

import java.sql.ResultSet;
import java.util.HashSet;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.cache.ConversionTable;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Builds the matrices with characterization factors for a given set of flows
 * and LCIA categories.
 */
public final class ImpactBuilder {

	private final IDatabase db;
	private final ConversionTable conversions;

	private boolean withUncertainties = false;

	// shared variables of the build methods
	private FlowIndex flowIndex;
	private DIndex<ImpactCategoryDescriptor> impactIndex;
	private FormulaInterpreter interpreter;
	private MatrixBuilder matrix;
	private UMatrix uncertainties;

	public ImpactBuilder(IDatabase db) {
		this.db = db;
		conversions = ConversionTable.create(db);
	}

	public ImpactBuilder withUncertainties(boolean b) {
		this.withUncertainties = b;
		return this;
	}

	public ImpactData build(
			FlowIndex flowIndex,
			DIndex<ImpactCategoryDescriptor> impactIndex,
			FormulaInterpreter interpreter) {

		this.flowIndex = flowIndex;
		this.impactIndex = impactIndex;
		this.interpreter = interpreter;

		// allocate and fill the matrices
		matrix = new MatrixBuilder();
		matrix.minSize(impactIndex.size(), flowIndex.size());
		uncertainties = withUncertainties ? new UMatrix() : null;
		if (flowIndex.isRegionalized) {
			fillRegionalized();
		} else {
			fill();
		}

		ImpactData data = new ImpactData();
		data.flowIndex = flowIndex;
		data.impactIndex = impactIndex;
		data.impactMatrix = matrix.finish();
		data.impactUncertainties = uncertainties;
		return data;
	}

	private void fill() {
		try {
			NativeSql.on(db).query(query(), r -> {

				long impactID = r.getLong(1);
				long flowID = r.getLong(2);
				long locationID = r.getLong(11);

				// check that the LCIA category
				if (!impactIndex.contains(impactID))
					return true;

				if (locationID > 0) {
					// skip regionalized factors in non-
					// regionalized calculations
					return true;
				}

				if (!flowIndex.contains(flowID))
					return true;

				// create the factor instance
				CalcImpactFactor f = new CalcImpactFactor();
				f.imactCategoryId = impactID;
				f.flowId = flowID;
				f.amount = r.getDouble(3);
				f.formula = r.getString(4);
				f.conversionFactor = getConversionFactor(r);
				f.isInput = flowIndex.isInput(flowID);

				// set the matrix value
				int row = impactIndex.of(impactID);
				int col = flowIndex.of(flowID);
				matrix.set(row, col, f.matrixValue(interpreter));

				// set possible uncertainties
				if (uncertainties != null) {
					int uType = r.getInt(7);
					if (!r.wasNull()) {
						f.uncertaintyType = UncertaintyType.values()[uType];
						f.parameter1 = r.getDouble(8);
						f.parameter2 = r.getDouble(9);
						f.parameter3 = r.getDouble(10);
					}
					uncertainties.add(row, col, f);
				}

				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to query impact factors", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void fillRegionalized() {

		// the default characterization factors are used for flow-location
		// pairs for which no specific characterization factor could be found
		TLongObjectHashMap<CalcImpactFactor>[] defaults = new TLongObjectHashMap[impactIndex.size()];
		for (int i = 0; i < impactIndex.size(); i++) {
			defaults[i] = new TLongObjectHashMap<>();
		}
		HashSet<LongPair> added = new HashSet<>();

		try {
			NativeSql.on(db).query(query(), r -> {

				long impactID = r.getLong(1);
				long flowID = r.getLong(2);
				long locationID = r.getLong(11);

				// check that the LCIA category
				if (!impactIndex.contains(impactID))
					return true;

				boolean isDefault = locationID == 0L;
				boolean addIt = true;
				if (!flowIndex.contains(flowID, locationID)) {
					if (!isDefault)
						return true;
					addIt = false;
				}

				// create the factor instance
				CalcImpactFactor f = new CalcImpactFactor();
				f.imactCategoryId = impactID;
				f.flowId = flowID;
				f.amount = r.getDouble(3);
				f.formula = r.getString(4);
				f.conversionFactor = getConversionFactor(r);
				f.isInput = flowIndex.isInput(flowID, locationID);
				if (uncertainties != null) {
					int uType = r.getInt(7);
					if (!r.wasNull()) {
						f.uncertaintyType = UncertaintyType.values()[uType];
						f.parameter1 = r.getDouble(8);
						f.parameter2 = r.getDouble(9);
						f.parameter3 = r.getDouble(10);
					}
				}

				int row = impactIndex.of(impactID);
				if (isDefault) {
					defaults[row].put(flowID, f);
				}
				if (addIt) {
					int col = flowIndex.of(flowID, locationID);
					matrix.set(row, col, f.matrixValue(interpreter));
					if (uncertainties != null) {
						uncertainties.add(row, col, f);
					}
					added.add(LongPair.of(flowID, locationID));
				}
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to query impact factors", e);
		}

		// set default factors where necessary
		if (added.size() == flowIndex.size())
			return;
		flowIndex.each((col, f) -> {
			long flowID = f.flow.id;
			long locationID = f.location != null
					? f.location.id
					: 0L;
			if (added.contains(LongPair.of(flowID, locationID)))
				return;
			for (int row = 0; row < defaults.length; row++) {
				CalcImpactFactor factor = defaults[row].get(flowID);
				if (factor == null)
					continue;
				matrix.set(row, col, factor.matrixValue(interpreter));
				if (uncertainties != null) {
					uncertainties.add(row, col, factor);
				}
			}
		});
	}

	private String query() {
		return "SELECT"
				+ /* 1 */ " f_impact_category,"
				+ /* 2 */ " f_flow,"
				+ /* 3 */ " value,"
				+ /* 4 */ " formula,"
				+ /* 5 */ " f_flow_property_factor,"
				+ /* 6 */ " f_unit,"
				+ /* 7 */ " distribution_type,"
				+ /* 8 */ " parameter1_value,"
				+ /* 9 */ " parameter2_value,"
				+ /* 10 */ " parameter3_value,"
				+ /* 11 */ " f_location"
				+ " FROM tbl_impact_factors";
	}

	private double getConversionFactor(ResultSet r) {
		try {
			double propFactor = conversions.getPropertyFactor(r.getLong(5));
			double unitFactor = conversions.getUnitFactor(r.getLong(6));
			if (unitFactor == 0)
				return 0;
			return propFactor / unitFactor;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A subset of MatrixData that contains only the LCIA matrix data. See the
	 * MatrixData class for the meaning of these fields.
	 */
	public static class ImpactData {
		public FlowIndex flowIndex;
		public DIndex<ImpactCategoryDescriptor> impactIndex;
		public IMatrix impactMatrix;
		public UMatrix impactUncertainties;
	}
}
