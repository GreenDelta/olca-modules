package org.openlca.geo.calc;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.geojson.Point;

public class WebMercatorTest {

    @Test
    public void testProjectUnproject() {
        for (int zoom = 0; zoom < 10; zoom++) {
            for (int lon = -180; lon <= 180; lon+=10) {
                for (int lat = -80; lat <= 80; lat+=10) {
                    Point p = new Point();
                    p.x = lon;
                    p.y = lat;
					WebMercator.apply(p, zoom);
					WebMercator.inverse(p, zoom);
                    Assert.assertEquals(lon, p.x, 1e-10);
                    Assert.assertEquals(lat, p.y, 1e-10);
                }
            }
        }
    }

}
