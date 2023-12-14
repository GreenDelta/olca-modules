package org.openlca.geo.lcia;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.geo.calc.IntersectionShare;
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

public class GeoFactorCalculator {

	private final IDatabase db;
	private final GeoFactorSetup setup;
	private final List<Location> locations;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private GeoFactorCalculator(
			IDatabase db, GeoFactorSetup setup, List<Location> locations) {
		this.db = db;
		this.setup = setup;
		this.locations = locations;
	}

	public static GeoFactorCalculator of(
			IDatabase db, GeoFactorSetup setup, List<Location> locations) {
		return new GeoFactorCalculator(db, setup, locations);
	}

	public List<ImpactFactor> calculate() {
		if (db == null || setup == null || locations == null) {
			log.error("invalid input");
			return List.of();
		}
		if (setup.bindings.isEmpty()
				|| setup.features.isEmpty()
				|| locations.isEmpty()) {
			log.warn("no flow bindings or locations; nothing to do");
			return List.of();
		}

		var params = parameterValuesOf(setup.features);
		return factorsOf(params);
	}

	/**
	 * Calculates the parameter values for the given locations from the respective
	 * intersections with the given feature collection and the aggregation function
	 * that is defined in the respective parameter.
	 */
	private Map<Location, List<PropVal>> parameterValuesOf(FeatureCollection coll) {

		// calculate intersections
		var calc = IntersectionCalculator.on(coll);
		Map<Location, List<IntersectionShare>> intersections = locations
				.parallelStream()
				.map(loc -> Pair.of(loc, calc.shares(loc)))
				.collect(Collectors.toMap(p -> p.first, p -> p.second));

		// calculate parameter values based on intersections
		var params = new HashMap<Location, List<PropVal>>();
		intersections.forEach((loc, shares) -> {

			var values = new ArrayList<PropVal>();
			params.put(loc, values);
			for (var param : setup.properties) {
				if (shares.isEmpty()) {
					values.add(PropVal.defaultOf(param));
					continue;
				}

				var featureValues = new ArrayList<Double>();
				var featureShares = new ArrayList<Double>();
				for (var fs : shares) {
					var f = fs.origin();
					var share = fs.value();
					if (f.properties == null)
						continue;
					var valObj = f.properties.get(param.name);
					if (!(valObj instanceof Number num))
						continue;
					featureValues.add(num.doubleValue());
					featureShares.add(share);
				}

				values.add(PropVal.of(param, featureValues, featureShares));
			}
		});
		return params;
	}

	private List<ImpactFactor> factorsOf(Map<Location, List<PropVal>> params) {

		var factors = new ArrayList<ImpactFactor>();

		// generate the non-regionalized default factors
		// for setup flows that are not yet present
		var fi = new FormulaInterpreter();
		for (var param : setup.properties) {
			fi.bind(param.identifier, param.defaultValue);
		}
		for (var b : setup.bindings) {
			if (b.flow == null)
				continue;
			try {
				double val = fi.eval(b.formula);
				factors.add(ImpactFactor.of(b.flow, val));
			} catch (Exception e) {
				log.error("failed to evaluate formula {} "
						+ " of binding with flow {}", b.formula, b.flow);
			}
		}

		// generate regionalized factors
		for (var loc : params.keySet()) {

			// bind the location specific parameter values
			// to a formula interpreter
			fi = new FormulaInterpreter();
			var propVals = params.get(loc);
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
					var factor = ImpactFactor.of(b.flow, val);
					factor.location = loc;
					factors.add(factor);
				} catch (Exception e) {
					log.error("Failed to calculate factor from formula "
							+ b.formula + " in binding with flow " + b.flow, e);
				}
			}
		}
		return factors;
	}
}
