package org.openlca.geo.calc;

import org.openlca.geo.Shape;
import org.openlca.geo.geojson.FeatureCollection;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

/**
 * Validates the geometries of a feature collection.
 */
public class FeatureValidation implements Runnable {

	private final FeatureCollection coll;
	private final Stats stats;
	private final AtomicBoolean canceled;
	private IntConsumer listener;

	private FeatureValidation(FeatureCollection coll) {
		this.coll = Objects.requireNonNull(coll);
		this.stats = Stats.create();
		this.canceled = new AtomicBoolean(false);
	}

	public static FeatureValidation of(FeatureCollection coll) {
		return new FeatureValidation(coll);
	}

	public int count() {
		return coll.features.size();
	}

	public void cancel() {
		canceled.set(true);
	}

	/**
	 * Reports the number of validated features after each
	 * validation event.
	 */
	public void onValidated(IntConsumer listener) {
		this.listener = listener;
	}

	public Stats stats() {
		return stats;
	}

	public boolean wasCanceled() {
		return canceled.get();
	}

	@Override
	public void run() {
		int count = 0;
		for (var f : coll.features) {
			if (canceled.get())
				break;
			count++;
			if (f.geometry == null) {
				next(count, Shape.UNKNOWN, false);
				continue;
			}
			var geo = JTS.fromGeoJSON(f.geometry);
			if (geo == null) {
				next(count, Shape.UNKNOWN, false);
				continue;
			}
			var shape = Shape.of(f.geometry);
			next(count, shape, geo.isValid());
		}
	}

	private void next(int count, Shape shape, boolean valid) {
		if (listener != null) {
			listener.accept(count);
		}
		if (valid) {
			stats.addValid(shape);
		} else {
			stats.addInvalid(shape);
		}
	}

	public record Stats(
			Map<Shape, Integer> validShapes,
			Map<Shape, Integer> invalidShapes
	) {

		static Stats create() {
			var valid = new EnumMap<Shape, Integer>(Shape.class);
			var invalid = new EnumMap<Shape, Integer>(Shape.class);
			return new Stats(valid, invalid);
		}

		void addValid(Shape shape) {
			add(validShapes, shape);
		}

		void addInvalid(Shape shape) {
			add(invalidShapes, shape);
		}

		private void add(Map<Shape, Integer> m, Shape shape) {
			if (m == null || shape == null)
				return;
			m.compute(shape, (s, count) ->
					count == null ? 1 : count + 1);
		}

		public int validCountOf(Shape shape) {
			return countOf(validShapes, shape);
		}

		public int invalidCountOf(Shape shape) {
			return countOf(invalidShapes, shape);
		}

		private int countOf(Map<Shape, Integer> m, Shape shape) {
			if (m == null || shape == null)
				return 0;
			var c = m.get(shape);
			return c == null ? 0 : c;
		}

		public int totalValid() {
			return totalOf(validShapes);
		}

		public int totalInvalid() {
			return totalOf(invalidShapes);
		}

		public int total() {
			return totalValid() + totalInvalid();
		}

		private int totalOf(Map<Shape, Integer> m) {
			if (m == null)
				return 0;
			int count = 0;
			for (var shape : Shape.values()) {
				var c = m.get(shape);
				if (c != null) {
					count += c;
				}
			}
			return count;
		}

		@Override
		public String toString() {
			var text = new StringBuilder(
					"Geometry             | Valid      | Invalid\n");
			for (var shape : Shape.values()) {
				var s = shape.toString();
				var v = validCountOf(shape);
				var i = invalidCountOf(shape);
				text.append(String.format(
						"%-20s | %-10d | %d%n", s, v, i));
			}
			return text.toString();
		}
	}
}
