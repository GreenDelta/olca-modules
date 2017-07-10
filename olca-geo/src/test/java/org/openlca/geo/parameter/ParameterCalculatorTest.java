package org.openlca.geo.parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.geotools.data.DataStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ImpactMethod.ParameterMean;
import org.openlca.geo.Tests;
import org.openlca.geo.kml.KmlFeature;

public class ParameterCalculatorTest {

	private DataStore dataStore;
	private IntersectionsCalculator intersectionsCalculator;
	private FeatureCalculator featureCalculator;

	@Before
	public void setUp() throws Exception {
		ShapeFileFolder repository = Tests.getRepository();
		dataStore = repository.openDataStore("states");
		intersectionsCalculator = new IntersectionsCalculator(dataStore);
		featureCalculator = new FeatureCalculator(dataStore,
				Collections.emptyMap(), ParameterMean.WEIGHTED_MEAN);
	}

	@After
	public void tearDown() throws Exception {
		dataStore.dispose();
	}

	@Test
	public void testPoint() throws Exception {
		// a point in New Mexico; DRAWSEQ = 42
		KmlFeature feature = KmlFeature.parse(Tests.getKml("point.kml"));
		Map<String, Double> shares = intersectionsCalculator.calculate(feature);
		Map<String, Double> params = featureCalculator
				.calculate(feature, Arrays.asList("DRAWSEQ"), shares);
		Assert.assertTrue(params.size() == 1);
		Assert.assertEquals(42, params.get("DRAWSEQ"), 1e-17);
	}

	@Test
	public void testLine() throws Exception {
		// a line that crosses
		// New Mexico; DRAWSEQ = 42
		// Texas; DRAWSEQ = 41
		// Oklahoma; DRAWSEQ = 38
		// Kansas; DRAWSEQ = 34
		KmlFeature feature = KmlFeature.parse(Tests.getKml("line.kml"));
		Map<String, Double> shares = intersectionsCalculator.calculate(feature);
		Map<String, Double> params = featureCalculator
				.calculate(feature, Arrays.asList("DRAWSEQ"), shares);
		double val = params.get("DRAWSEQ");
		Assert.assertTrue(34 < val && val < 42);
	}

	@Test
	public void testPolygon() throws Exception {
		// a polygon that intersects
		// New Mexico; DRAWSEQ = 42
		// Texas; DRAWSEQ = 41
		// Oklahoma; DRAWSEQ = 38
		// Kansas; DRAWSEQ = 34
		// Colorado; DRAWSEQ = 32
		KmlFeature feature = KmlFeature.parse(Tests.getKml("polygon.kml"));
		Map<String, Double> shares = intersectionsCalculator.calculate(feature);
		Map<String, Double> params = featureCalculator
				.calculate(feature, Arrays.asList("DRAWSEQ"), shares);
		double val = params.get("DRAWSEQ");
		Assert.assertTrue(32 < val && val < 42);
	}

	@Test
	public void testMultiPoint() throws Exception {
		// two points
		// New Mexico; DRAWSEQ = 42
		// Kansas; DRAWSEQ = 34
		KmlFeature feature = KmlFeature.parse(Tests.getKml("multipoint.kml"));
		Map<String, Double> shares = intersectionsCalculator.calculate(feature);
		Map<String, Double> params = featureCalculator
				.calculate(feature, Arrays.asList("DRAWSEQ"), shares);
		Assert.assertTrue(params.size() == 1);
		Assert.assertEquals(38, params.get("DRAWSEQ"), 1e-17);
	}

}
