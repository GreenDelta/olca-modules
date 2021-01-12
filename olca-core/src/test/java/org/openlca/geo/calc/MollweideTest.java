package org.openlca.geo.calc;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.geojson.Point;

public class MollweideTest {

	/**
	 * Calculates the example table from: Map Projections – A Working Manual,
	 * USGS Professional Paper 1395, John P. Snyder, 1987, pp. 249–252
	 *
	 * https://pubs.usgs.gov/pp/1395/report.pdf
	 */
	@Test
	public void testMeridian90() {
		double[][] data = new double[][]{
			{90, .00000, 1.00000},
			{85, .20684, .97837},
			{80, .32593, .94539},
			{75, .42316, .90606},
			{70, .50706, .86191},
			{65, .58111, .81382},
			{60, .64712, .76239},
			{55, .70617, .70804},
			{50, .75894, .65116},
			{45, .80591, .59204},
			{40, .84739, .53097},
			{35, .88362, .46820},
			{30, .91477, .40397},
			{25, .94096, .33850},
			{20, .96229, .27201},
			{15, .97882, .20472},
			{10, .99060, .13681},
			{5, .99765, .06851},
			{0, 1.00000, .00000},
		};
		var projection = new Mollweide(Math.sqrt(0.5));
		for (var datum : data) {
			var lat = datum[0];
			var x = datum[1];
			var y = datum[2];
			var point = new Point(90, lat);
			projection.apply(point);
			assertEquals(x, point.x, 1e-4);
			assertEquals(y, point.y, 1e-4);
		}
	}

	@Test
	public void testFullRange() {
		var mollweide = new Mollweide();
		for (int lon = -180; lon <= 180; lon+=10) {
			for (int lat = -90; lat <= 90; lat+=10) {
				Point p = new Point();
				p.x = lon;
				p.y = lat;
				mollweide.apply(p);
				mollweide.inverse(p);
				Assert.assertEquals(lon, p.x, 1e-10);
				Assert.assertEquals(lat, p.y, 1e-10);
			}
		}
	}
}
