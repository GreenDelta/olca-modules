package org.openlca.geo.geojson;

import java.io.IOException;

import com.google.gson.JsonObject;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

public final class Point extends Geometry {

	/**
	 * The x-coordinate is the longitude of the point.
	 */
	public double x;

	/**
	 * The y-coordinate is the latitude of the point.
	 */
	public double y;

	public Point() {
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	static Point fromJson(JsonObject obj) {
		return Coordinates.readPoint(
				obj.get("coordinates"));
	}

	@Override
	JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Point");
		obj.add("coordinates",
				Coordinates.writePoint(this));
		return obj;
	}

	@Override
	public Point clone() {
		Point clone = new Point();
		clone.x = x;
		clone.y = y;
		return clone;
	}

	@Override
	public String toString() {
		return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	void pack(MessagePacker packer) {
		try {
			packer.packString("type");
			packer.packString("Point");
			packer.packString("coordinates");
			packer.packArrayHeader(2);
			packer.packDouble(x);
			packer.packDouble(y);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

