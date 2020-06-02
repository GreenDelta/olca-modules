package org.openlca.geo.geojson;

import java.io.StringReader;
import java.io.StringWriter;

public class MsgPackBenchmark {

	private static final int SIZE = 1000;
	private static final int ITERATIONS = 50_000;

	public static void main(String[] args) {

		MultiPoint multiPoint = new MultiPoint();
		for (int i = 0; i < SIZE; i++) {
			Point point = new Point();
			point.x = i;
			point.y = i;
			multiPoint.points.add(point);
		}
		FeatureCollection coll = FeatureCollection.of(multiPoint);

		System.out.print("MsgPack; warm up ...");
		int t = (int) msgPack(coll, ITERATIONS);
		System.out.println("  took " + t + " ms");

		System.out.print("MsgPack; benchmark ...");
		t = (int) msgPack(coll, ITERATIONS);
		System.out.println("  took " + t + " ms");

		System.out.print("JSON; warm up ...");
		t = (int) json(coll, ITERATIONS);
		System.out.println("  took " + t + " ms");

		System.out.print("JSON; benchmark ...");
		t = (int) json(coll, ITERATIONS);
		System.out.println("  took " + t + " ms");
	}

	private static double json(FeatureCollection coll, int iterations) {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
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

	private static double msgPack(FeatureCollection coll, int iterations) {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] data = MsgPack.pack(coll);
			FeatureCollection r = MsgPack.unpack(data);
			if (((MultiPoint) r.features.get(0).geometry).points.size() != SIZE) {
				throw new RuntimeException("invalid result");
			}
		}
		long time = System.nanoTime() - start;
		return time / 1e6;
	}
}
