package org.openlca.geo.lcia;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class PropValTest {

	private GeoProperty prop;

	@Before
	public void setup() {
		prop = new GeoProperty();
		prop.defaultValue = 42;
	}

	@Test
	public void testDefault() {
		var vs = List.of(
				PropVal.defaultOf(prop),
				PropVal.of(prop, null, null),
				PropVal.of(prop, List.of(), List.of()));
		for (var v : vs) {
			assertEquals(prop, v.param());
			assertEquals(42.0, v.value(), 1e-16);
		}
	}

	@Test
	public void testMin() {
		prop.aggregation = GeoAggregation.MINIMUM;
		var vs = List.of(
				List.of(1.0),
				List.of(1.0, 2.0, 3.0),
				List.of(2.0, 1.0, 3.0),
				List.of(3.0, 2.0, 1.0));
		for (var v : vs) {
			var pv = PropVal.of(prop, v, List.of(1.0));
			assertEquals(1.0, pv.value(), 1e-16);
		}
	}

	@Test
	public void testMax() {
		prop.aggregation = GeoAggregation.MAXIMUM;
		var vs = List.of(
				List.of(3.0),
				List.of(1.0, 2.0, 3.0),
				List.of(2.0, 3.0, 1.0),
				List.of(3.0, 2.0, 1.0));
		for (var v : vs) {
			var pv = PropVal.of(prop, v, List.of(1.0));
			assertEquals(3.0, pv.value(), 1e-16);
		}
	}

	@Test
	public void testMean() {
		prop.aggregation = GeoAggregation.AVERAGE;
		var vs = List.of(
				List.of(2.0),
				List.of(1.0, 2.0, 3.0));
		for (var v : vs) {
			var pv = PropVal.of(prop, v, List.of(1.0));
			assertEquals(2.0, pv.value(), 1e-16);
		}
	}

	@Test
	public void testWeightedMean() {
		prop.aggregation = GeoAggregation.WEIGHTED_AVERAGE;
		var v1 = PropVal.of(prop, List.of(1.0), List.of(1.0));
		assertEquals(1.0, v1.value(), 1e-16);
		var v2 = PropVal.of(prop,
				List.of(1.0, 2.0, 3.0),
				List.of(0.75, 0.25, 1.5));
		assertEquals(2.3, v2.value(), 1e-16);
	}
}
