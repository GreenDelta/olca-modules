package org.openlca.geo.calc;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

/**
 * Tries to fix invalid geometries of the features in the collection in place.
 * Currently, this is only done for self-intersecting polygons because the
 * intersection calculation would fail for them. We currently apply the
 * approach <a href="https://stackoverflow.com/a/31474580">here</a>. An
 * alternative is to run {@code buffer(0)} on such polygons as described
 * <a href="https://github.com/locationtech/jts/issues/657">here</a>.
 */
public class FeatureRepair implements Runnable {

	public enum PolygonFix {

		/**
		 * When there are self intersections, split the polygons at intersection
		 * points into a multi-polygon.
		 */
		SPLIT,

		/**
		 * When there are self intersections, calculate an outer polygon.
		 */
		WRAP,

	}

	private final FeatureCollection coll;
	private final PolygonFix strategy;
	private final AtomicBoolean cancelled;

	private IntConsumer listener;
	private int handled = 0;

	private FeatureRepair(FeatureCollection coll, PolygonFix strategy) {
		this.coll = coll;
		this.strategy = strategy;
		cancelled = new AtomicBoolean(false);
	}

	public static FeatureRepair of(FeatureCollection coll) {
		return new FeatureRepair(coll, PolygonFix.WRAP);
	}

	public static FeatureRepair of(FeatureCollection coll, PolygonFix strategy) {
		return new FeatureRepair(coll, strategy);
	}

	public int count() {
		return coll.features.size();
	}

	public void cancel() {
		cancelled.set(true);
	}

	/**
	 * Reports the number of handled features after each
	 * feature that was checked.
	 */
	public void onHandled(IntConsumer listener) {
		this.listener = listener;
	}

	public boolean wasCancelled() {
		return cancelled.get();
	}

	private void reportNext() {
		handled++;
		if (listener != null) {
			listener.accept(handled);
		}
	}

	@Override
	public void run() {
		if (coll == null)
			return;

		var fixed = new ArrayList<Feature>();
		for (var f : coll.features) {
			if (cancelled.get())
				break;
			var geo = JTS.fromGeoJSON(f.geometry);
			if (geo == null) {
				reportNext();
				continue;
			}

			if (geo.isValid()) {
				geo.normalize();
			} else if (strategy == PolygonFix.SPLIT) {
				geo = splitIntersections(geo);
			} else {
				geo = wrapIntersections(geo);
			}
			reportNext();

			if (geo == null)
				continue;
			var feature = new Feature();
			feature.geometry = JTS.toGeoJSON(geo);
			feature.properties = f.properties;
			fixed.add(feature);
		}

		if (!cancelled.get()) {
			coll.features.clear();
			coll.features.addAll(fixed);
		}
	}

	private Geometry wrapIntersections(Geometry geom) {
		if (geom instanceof Polygon)
			return geom.buffer(0);

		if (!(geom instanceof MultiPolygon mul))
			return geom;

		var polys = new ArrayList<Polygon>();
		for (int n = 0; n < mul.getNumGeometries(); n++) {
			var gi = geom.getGeometryN(n).buffer(0);
			if (gi instanceof Polygon poly) {
				polys.add(poly);
			}
		}
		return new MultiPolygon(polys.toArray(Polygon[]::new), geom.getFactory());
	}

	private Geometry splitIntersections(Geometry geom) {

		if (geom instanceof Polygon polygon) {
			var polygonizer = new Polygonizer();
			addPolygon(polygon, polygonizer);
			return makeGeometry(polygonizer);
		}

		if (geom instanceof MultiPolygon) {
			var polygonizer = new Polygonizer();
			for (int i = geom.getNumGeometries(); i > 0; i--) {
				if (geom.getGeometryN(i - 1) instanceof Polygon polygon) {
					addPolygon(polygon, polygonizer);
				}
			}
			return makeGeometry(polygonizer);
		}

		return geom;
	}

	private void addPolygon(Polygon polygon, Polygonizer p) {
		addLine(polygon.getExteriorRing(), p);
		for (int i = polygon.getNumInteriorRing(); i > 0; i--) {
			addLine(polygon.getInteriorRingN(i - 1), p);
		}
	}

	private void addLine(LineString line, Polygonizer p) {
		if (line instanceof LinearRing) {
			line = line.getFactory()
					.createLineString(line.getCoordinateSequence());
		}
		// makes any self intersections explicit
		var point = line.getFactory().createPoint(line.getCoordinateN(0));
		var geometry = line.union(point);
		p.add(geometry);
	}

	private Geometry makeGeometry(Polygonizer p) {
		var it = p.getPolygons().iterator();
		if (!it.hasNext())
			return null;
		if (!(it.next() instanceof Geometry g))
			return null;
		while (it.hasNext()) {
			if (it.next() instanceof Geometry next) {
				g = g.symDifference(next);
			}
		}
		return g;
	}
}
