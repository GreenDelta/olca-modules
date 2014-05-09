package org.openlca.geo;

import org.geotools.data.DataStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class ParameterCalculatorTest {

	private DataStore dataStore;
	private ParameterCalculator calculator;

	@Before
	public void setUp() throws Exception {
		ShapeFileRepository repository = Tests.getRepository();
		dataStore = repository.openDataStore("states");
		calculator = new ParameterCalculator(dataStore);
	}

	@After
	public void tearDown() throws Exception {
		dataStore.dispose();
	}

	@Test
	public void testPoint() throws Exception {
		// a point in New Mexico; DRAWSEQ = 42
		KmlFeature feature = Tests.getKmlFeature("point.kml");
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
		KmlFeature feature = Tests.getKmlFeature("line.kml");
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
		KmlFeature feature = Tests.getKmlFeature("polygon.kml");
		Map<String, Double> params = calculator.calculate(feature,
				Arrays.asList("DRAWSEQ"));
		double val = params.get("DRAWSEQ");
		Assert.assertTrue(32 < val && val < 42);
	}
}
