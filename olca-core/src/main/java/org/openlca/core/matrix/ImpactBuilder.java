package org.openlca.core.matrix;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.cache.ConversionTable;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Builds the matrices with characterization factors for a given set of flows
 * and LCIA categories.
 */
public final class ImpactBuilder {

	private final IDatabase db;
	private final EnviIndex flowIndex;
	private final ImpactIndex impactIndex;
	private final FormulaInterpreter interpreter;
	private final boolean withUncertainties;

	private final ConversionTable conversions;
	private MatrixBuilder matrix;
	private UMatrix uncertainties;

	private ImpactBuilder(Config config) {
		this.db = config.db;
		this.flowIndex = config.flows;

		// if no impact index is set via the configuration,
		// we build it from all impacts in the database.
		if (config.impacts != null) {
			this.impactIndex = config.impacts;
		} else {
			var all = new ImpactCategoryDao(db).getDescriptors();
			this.impactIndex = ImpactIndex.of(all);
		}

		// same for the interpreter, we create it if it is not
		// set via the configuration
		if (config.interpreter != null) {
			this.interpreter= config.interpreter;
		} else {
			var contexts = new TreeSet<Long>();
			impactIndex.each((i, d) -> contexts.add(d.id));
			this.interpreter = ParameterTable.interpreter(
				db, contexts, Collections.emptyList());
		}

		withUncertainties = config.withUncertainties;
		conversions = ConversionTable.create(db);
	}

	public static Config of(IDatabase db, EnviIndex flows) {
		return new Config(db, flows);
	}

	public static Config of(MatrixConfig config, EnviIndex flows) {
		return new Config(config, flows);
	}

	public ImpactData build() {

		// allocate and fill the matrices
		matrix = new MatrixBuilder();
		matrix.minSize(impactIndex.size(), flowIndex.size());
		uncertainties = withUncertainties
			? new UMatrix()
			: null;
		if (flowIndex.isRegionalized()) {
			fillRegionalized();
		} else {
			fill();
		}

		// add factors for virtual impact flows
		flowIndex.each((flowIdx, enviFlow) -> {
			if (!enviFlow.isVirtual())
				return;
			if (enviFlow.wrapped() instanceof ImpactDescriptor impact) {
				int impactIdx = impactIndex.of(impact);
				if (impactIdx >= 0) {
					matrix.set(impactIdx, flowIdx, 1);
				}
			}
		});

		var data = new ImpactData();
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
				var f = new CalcImpactFactor();
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
				var f = new CalcImpactFactor();
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
			long flowID = f.flow().id;
			long locationID = f.location() != null
					? f.location().id
					: 0L;
			if (added.contains(LongPair.of(flowID, locationID)))
				return;
			for (int row = 0; row < defaults.length; row++) {
				CalcImpactFactor factor = defaults[row].get(flowID);
				if (factor == null)
					continue;
				factor.isInput = f.isInput();
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
		public EnviIndex flowIndex;
		public ImpactIndex impactIndex;
		public Matrix impactMatrix;
		public UMatrix impactUncertainties;

		/**
		 * Adds the impact data to the given matrix data.
		 */
		public void addTo(MatrixData data) {
			if (data == null)
				return;
			if (data.enviIndex != flowIndex) {
				// this would be a strange setup as the impact
				// data are built with a corresponding flow index
				// and this should be identical to the flow index
				// in the data.
				data.enviIndex = flowIndex;
			}
			data.impactIndex = impactIndex;
			data.impactMatrix = impactMatrix;
			data.impactUncertainties = impactUncertainties;
		}
	}

	public static class Config {

		private final IDatabase db;
		private final EnviIndex flows;
		private boolean withUncertainties;
		private FormulaInterpreter interpreter;
		private ImpactIndex impacts;

		private Config(IDatabase db, EnviIndex flows) {
			this.db = db;
			this.flows = flows;
		}

		public Config(MatrixConfig conf, EnviIndex flows) {
			this.db	= conf.db;
			this.flows = flows;
			this.withUncertainties = conf.withUncertainties;
			this.interpreter = conf.interpreter;
			this.impacts = conf.impactIndex;
		}

		public Config withUncertainties(boolean b) {
			this.withUncertainties = b;
			return this;
		}

		public Config withInterpreter(FormulaInterpreter interpreter) {
			this.interpreter = interpreter;
			return this;
		}

		public Config withImpacts(ImpactIndex impacts) {
			this.impacts = impacts;
			return this;
		}

		public Config withImpacts(Iterable<ImpactDescriptor> impacts) {
			this.impacts = ImpactIndex.of(impacts);
			return this;
		}

		public Config withImpacts(ImpactMethodDescriptor method) {
			this.impacts = ImpactIndex.of(db, method);
			return this;
		}

		public ImpactData build() {
			return new ImpactBuilder(this).build();
		}
	}
}
