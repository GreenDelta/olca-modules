package org.openlca.geo.multi;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.GeometryCollection;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

public class JakToGeotools {

	public static GeometryCollection convert(List<Geometry> geometries) {
		GeometryBuilder builder = new GeometryBuilder();
		com.vividsolutions.jts.geom.Geometry[] converted = new com.vividsolutions.jts.geom.Geometry[geometries
				.size()];
		int count = 0;
		// only allow homogeneous collections (Geometry.intersection is only
		// working on homogeneous collections)
		Class<? extends Geometry> collectionType = null;
		for (Geometry geometry : geometries) {
			if (collectionType == null)
				collectionType = geometry.getClass();
			else if (collectionType != geometry.getClass())
				return builder.geometryCollection();
			converted[count] = convert(geometry);
			count++;
		}
		return builder.geometryCollection(converted);
	}

	public static com.vividsolutions.jts.geom.Geometry convert(
			Geometry geometry) {
		if (geometry instanceof Point)
			return convert((Point) geometry);
		if (geometry instanceof LineString)
			return convert((LineString) geometry);
		if (geometry instanceof Polygon)
			return convert((Polygon) geometry);
		return null;
	}

	private static com.vividsolutions.jts.geom.Point convert(Point point) {
		GeometryBuilder builder = new GeometryBuilder();
		Coordinate coordinate = point.getCoordinates().get(0);
		if (coordinate == null)
			return builder.point();
		return builder.point(coordinate.getLongitude(),
				coordinate.getLatitude());
	}

	private static com.vividsolutions.jts.geom.LineString convert(
			LineString line) {
		GeometryBuilder builder = new GeometryBuilder();
		return builder.lineString(toDoubleArray(line.getCoordinates()));
	}

	private static com.vividsolutions.jts.geom.Polygon convert(Polygon polygon) {
		GeometryBuilder builder = new GeometryBuilder();
		return builder.polygon(toDoubleArray(polygon.getOuterBoundaryIs()
				.getLinearRing().getCoordinates()));
	}

	private static double[] toDoubleArray(List<Coordinate> coordinates) {
		if (coordinates == null || coordinates.isEmpty())
			return new double[0];
		List<Double> list = new ArrayList<>();
		for (Coordinate co : coordinates) {
			list.add(co.getLongitude());
			list.add(co.getLatitude());
		}
		double[] array = new double[list.size()];
		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);
		return array;
	}

}
