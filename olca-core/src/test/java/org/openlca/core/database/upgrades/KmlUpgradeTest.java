package org.openlca.core.database.upgrades;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Location;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.Point;
import org.openlca.util.BinUtils;

/**
 * Tests the upgrade of KML data into the new GeoData format.
 */
public class KmlUpgradeTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testUpgradeKml() {
		var location = db.insert(Location.of("B"));
		db.clearCache();

		// downgrade the database
		var u = new DbUtil(db);
		u.createColumn("tbl_locations", "kmz BLOB(32 M)");
		DbUtil.setVersion(db, 9);

		var kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
			"  <Placemark>\n" +
			"    <name>Simple placemark</name>\n" +
			"    <Point>\n" +
			"      <coordinates>-122.0822035425683,37.42228990140251,0</coordinates>\n" +
			"    </Point>\n" +
			"  </Placemark>\n" +
			"</kml>";
		var kmz = BinUtils.zip(kml.getBytes(StandardCharsets.UTF_8));

		// insert the kml
		var sql = "select id, kmz from tbl_locations";
		NativeSql.on(db).updateRows(sql, r -> {
			var id = r.getLong(1);
			if (id != location.id)
				return true;
			var blob = new ArrayInputStream(kmz);
			r.updateBlob(2, blob);
			r.updateRow();
			return true;
		});

		// run the upgrade
		db.clearCache();
		Upgrades.on(db);

		// check the location
		var upgradedLoc = db.get(Location.class, location.id);
		var features = GeoJSON.unpack(upgradedLoc.geodata);
		assertFalse(features.isEmpty());
		var geometry = features.first();
		assertNotNull(geometry);
		assertTrue(geometry.geometry instanceof Point);
		var point = (Point) geometry.geometry;
		assertEquals(-122.082, point.x, 1e-3);
		assertEquals(37.422, point.y, 1e-3);

		db.delete(location);
	}
}
