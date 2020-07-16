package org.openlca.geo.geojson;

import java.io.StringReader;
import java.io.StringWriter;

public class ProtoPackBenchmark {

	private static final int SIZE = 1000;
	private static final int ITERATIONS = 5000;

	public static void main(String[] args) {

		MultiPoint multiPoint = new MultiPoint();
		for (int i = 0; i < SIZE; i++) {
			Point point = new Point();
			point.x = i;
			point.y = i;
			multiPoint.points.add(point);
		}
		FeatureCollection coll = FeatureCollection.of(multiPoint);

		System.out.print("JSON; warm up ...");
		int t = (int) json(coll);
		System.out.println("  took " + t + " ms");

		System.out.print("JSON; benchmark ...");
		t = (int) json(coll);
		System.out.println("  took " + t + " ms");

		System.out.print("ProtoPack; warm up ...");
		t = (int) protoPack(coll);
		System.out.println("  took " + t + " ms");

		System.out.print("ProtoPack; benchmark ...");
		t = (int) protoPack(coll);
		System.out.println("  took " + t + " ms");
	}

	private static double json(FeatureCollection coll) {
		long start = System.nanoTime();
		for (int i = 0; i < ITERATIONS; i++) {
			StringWriter writer = new StringWriter();
			GeoJSON.write(coll, writer);
			writer.flush();
			StringReader reader = new StringReader(writer.toString());
			FeatureCollection r = GeoJSON.read(reader);
			if (((MultiPoint) r.features.get(0).geometry).points.size() != SIZE) {
				throw new RuntimeException("invalid result");
			}
		}
		long time = System.nanoTime() - start;
		return time / 1e6;
	}

	private static double protoPack(FeatureCollection coll) {
		long start = System.nanoTime();
		for (int i = 0; i < ITERATIONS; i++) {
			byte[] data = ProtoPack.pack(coll);
			var r = ProtoPack.unpack(data);
			if (((MultiPoint) r.features.get(0).geometry).points.size() != SIZE) {
				throw new RuntimeException("invalid result");
			}
		}
		long time = System.nanoTime() - start;
		return time / 1e6;
	}
}
