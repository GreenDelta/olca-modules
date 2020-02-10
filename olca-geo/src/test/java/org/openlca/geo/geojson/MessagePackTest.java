package org.openlca.geo.geojson;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;

public class MessagePackTest {

	@Test
	public void testPoint() {
		Point point = new Point(13.2, 52.4);
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		MsgPack.packPoint(point, packer);
		MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(
				packer.toByteArray());
		Point clone = MsgPack.unpackPoint(unpacker);
		Assert.assertEquals(13.2, clone.x, 1e-16);
		Assert.assertEquals(52.4, clone.y, 1e-16);
	}

	@Test
	public void testEmptyCollection() {
		FeatureCollection coll = new FeatureCollection();
		FeatureCollection clone = MsgPack.unpack(MsgPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertTrue(clone.features.isEmpty());
	}

	@Test
	public void testEmptyFeature() {
		FeatureCollection coll = new FeatureCollection();
		coll.features.add(new Feature());
		FeatureCollection clone = MsgPack.unpack(MsgPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertEquals(1, clone.features.size());
		Assert.assertNull(clone.features.get(0).geometry);
	}

	@Test
	public void testPackMap() throws IOException {
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packMapHeader(2);
		packer.packString("type");
		packer.packString("Feature");
		packer.packString("geometry");
		packer.packNil();

		byte[] data = packer.toByteArray();
		MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
		Assert.assertTrue(unpacker.hasNext());
		ImmutableValue val = unpacker.unpackValue();
		Assert.assertTrue(val.isMapValue());
	}

}
