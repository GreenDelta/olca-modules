package org.openlca.geo.lcia;

import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.geo.calc.FeatureShare;
import org.openlca.geo.calc.IntersectionCalculator;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeoFactorCalculator implements Runnable {

	private final IDatabase db;
	private final GeoFactorSetup setup;
	private final ImpactCategory impact;
	private final List<Location> locations;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public GeoFactorCalculator(
			IDatabase db, GeoFactorSetup setup, ImpactCategory impact, List<Location> locations
	) {
		this.db = db;
		this.setup = setup;
		this.impact = impact;
		this.locations = locations;
	}

	@Override
	public void run() {

		// check the input
		if (setup == null || impact == null) {
			log.error("no setup or LCIA category");
			return;
		}
		if (db == null) {
			log.error("no connected database");
			return;
		}
		if (setup.bindings.isEmpty()) {
			log.warn("no flow bindings; nothing to do");
			return;
		}

		// calculate the intersections, parameter values,
		// and finally generate the LCIA factors
		if (setup.features.isEmpty()) {
			log.error("no features available for the "
					+ "intersection calculation");
			return;
		}
		var params = calcParamVals(setup.features);
		createFactors(params);
	}

	/**
	 * Calculates the parameter values for the given locations from the respective
	 * intersections with the given feature collection and the aggregation function
	 * that is defined in the respective parameter.
	 */
	private Map<Location, List<PropVal>> calcParamVals(FeatureCollection coll) {

		// calculate intersections
		var calc = IntersectionCalculator.on(coll);
		Map<Location, List<FeatureShare>> intersections = locations
			.parallelStream()
			.map(loc -> Pair.of(loc, calc.shares(loc)))
			.collect(Collectors.toMap(p -> p.first, p -> p.second));

		// calculate parameter values based on intersections
		var locParams = new HashMap<Location, List<PropVal>>();
		intersections.forEach((loc, featureShares) -> {

			var paramVals = new ArrayList<PropVal>();
			locParams.put(loc, paramVals);
			for (var param : setup.properties) {
				if (featureShares.isEmpty()) {
					paramVals.add(PropVal.defaultOf(param));
					continue;
				}

				var vals = new ArrayList<Double>();
				var shares = new ArrayList<Double>();
				for (var fs : featureShares) {
						var f = fs.feature();
						var share = fs.value();
					if (f.properties == null)
						continue;
					var valObj = f.properties.get(param.name);
					if (!(valObj instanceof Number num))
						continue;
					vals.add(num.doubleValue());
					shares.add(share);
				}

				paramVals.add(PropVal.of(param, vals, shares));
			}
		});
		return locParams;
	}

	private void createFactors(Map<Location, List<PropVal>> locParams) {

		// remove all LCIA factors with a flow and location
		// that will be calculated
		var setupFlows = new TLongHashSet();
		for (var b : setup.bindings) {
			if (b.flow == null)
				continue;
			setupFlows.add(b.flow.id);
		}
		var setupLocations = new TLongHashSet();
		for (var loc : locations) {
			setupLocations.add(loc.id);
		}
		var isDefaultPresent = new TLongByteHashMap();
		var removals = new ArrayList<ImpactFactor>();
		for (var factor : impact.impactFactors) {
			if (factor.flow == null)
				return;
			long flowID = factor.flow.id;
			if (!setupFlows.contains(flowID))
				continue;
			if (factor.location == null) {
				isDefaultPresent.put(flowID, (byte) 1);
			} else if (setupLocations.contains(factor.location.id)) {
				removals.add(factor);
			}
		}
		impact.impactFactors.removeAll(removals);

		// generate the non-regionalized default factors
		// for setup flows that are not yet present
		var fi = new FormulaInterpreter();
		for (var param : setup.properties) {
			fi.bind(param.identifier, param.defaultValue);
		}
		for (var b : setup.bindings) {
			if (b.flow == null)
				continue;
			byte present = isDefaultPresent.get(b.flow.id);
			if (present == (byte) 1)
				continue;
			try {
				double val = fi.eval(b.formula);
				impact.factor(b.flow, val);
			} catch (Exception e) {
				log.error("failed to evaluate formula {} "
						+ " of binding with flow {}", b.formula, b.flow);
			}
		}

		// finally, generate regionalized factors
		for (var loc : locParams.keySet()) {

			// bind the location specific parameter values
			// to a formula interpreter
			fi = new FormulaInterpreter();
			var propVals = locParams.get(loc);
			if (propVals == null)
				continue;
			for (var v : propVals) {
				var param = v.param().identifier;
				fi.bind(param, v.value());
			}

			for (var b : setup.bindings) {
				if (b.flow == null || b.formula == null)
					continue;
				try {
					double val = fi.eval(b.formula);
					var factor = impact.factor(b.flow, val);
					factor.location = loc;
				} catch (Exception e) {
					log.error("Failed to calculate factor from formula "
							+ b.formula + " in binding with flow " + b.flow, e);
				}
			}
		}
	}
}
