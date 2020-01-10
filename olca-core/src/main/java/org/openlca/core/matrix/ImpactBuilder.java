package org.openlca.core.matrix;

import java.sql.ResultSet;

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
public class ImpactBuilder {

	private final IDatabase db;
	private final ConversionTable conversions;

	private boolean withUncertainties = false;

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

		ImpactData data = new ImpactData();
		data.enviIndex = flowIndex;
		data.impactIndex = impactIndex;

		// allocate matrices
		MatrixBuilder matrix = new MatrixBuilder();
		matrix.minSize(impactIndex.size(), flowIndex.size());
		UMatrix uncertainties = withUncertainties ? new UMatrix() : null;

		// collect factors
		try {
			NativeSql.on(db).query(query(), r -> {

				long impactID = r.getLong(1);
				long flowID = r.getLong(2);
				if (!impactIndex.contains(impactID))
					return true;
				if (!flowIndex.contains(flowID))
					return true;

				CalcImpactFactor f = new CalcImpactFactor();
				f.imactCategoryId = impactID;
				f.flowId = flowID;
				f.amount = r.getDouble(3);
				f.formula = r.getString(4);
				f.conversionFactor = getConversionFactor(r);

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
		} catch (
				Exception e) {
			throw new RuntimeException(
					"failed to query impact factors", e);
		}

		data.impactMatrix = matrix.finish();
		data.impactUncertainties = uncertainties;
		return data;
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
				+ /* 10 */ " parameter3_value"
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
		public FlowIndex enviIndex;
		public DIndex<ImpactCategoryDescriptor> impactIndex;
		public IMatrix impactMatrix;
		public UMatrix impactUncertainties;
	}
}
