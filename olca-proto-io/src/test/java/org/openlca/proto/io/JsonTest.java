package org.openlca.proto.io;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;

import com.google.protobuf.util.JsonFormat;

public class JsonTest {

	@Test
	public void testRefIO() throws Exception {
		var ref = ProtoRef.newBuilder()
			.setType(ProtoType.Process)
			.setId(UUID.randomUUID().toString())
			.setName("steel")
			.setDescription("some steel process")
			.setCategory("materials/metals")
			.build();

		var json = JsonFormat.printer().print(ref);
		var refCopy = ProtoRef.newBuilder();
		JsonFormat.parser().merge(json, refCopy);

		assertEquals(ProtoType.Process, refCopy.getType());
		assertEquals(ref.getId(), refCopy.getId());
		assertEquals("steel", refCopy.getName());
		assertEquals("some steel process", refCopy.getDescription());
		assertEquals("materials/metals", refCopy.getCategory());
	}

}
