package org.openlca.geo.parameter;

import org.geotools.data.DataStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.geo.Tests;
import org.openlca.geo.kml.KmlFeature;
import org.openlca.geo.kml.KmlTests;
import org.openlca.geo.parameter.FeatureCalculator;
import org.openlca.geo.parameter.ShapeFileRepository;

import java.util.Arrays;
import java.util.Map;

public class ParameterCalculatorTest {

	private DataStore dataStore;
	private FeatureCalculator calculator;

	@Before
	public void setUp() throws Exception {
		ShapeFileRepository repository = Tests.getRepository();
		dataStore = repository.openDataStore("states");
		calculator = new FeatureCalculator(dataStore);
	}

	@After
	public void tearDown() throws Exception {
		dataStore.dispose();
	}

	@Test
	public void testPoint() throws Exception {
		// a point in New Mexico; DRAWSEQ = 42
		KmlFeature feature = KmlTests.parse(Tests.getKml("point.kml"));
		Map<String, Double> params = calculator.calculate(feature,
				Arrays.asList("DRAWSEQ"));
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
		KmlFeature feature = KmlTests.parse(Tests.getKml("line.kml"));
		Map<String, Double> params = calculator.calculate(feature,
				Arrays.asList("DRAWSEQ"));
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
		KmlFeature feature = KmlTests.parse(Tests.getKml("polygon.kml"));
		Map<String, Double> params = calculator.calculate(feature,
				Arrays.asList("DRAWSEQ"));
		double val = params.get("DRAWSEQ");
		Assert.assertTrue(32 < val && val < 42);
	}

	@Test
	public void test() {
		KmlFeature feature = KmlTests.parse(Tests.getKml("temp.kml"));
		Map<String, Double> params =  calculator.calculate(feature, Arrays.asList("DRAWSEQ"));
		System.out.println();
	}

}
