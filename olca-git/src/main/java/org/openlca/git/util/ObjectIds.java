package org.openlca.git.util;

import java.io.ByteArrayInputStream;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.sha1.SHA1;

public class ObjectIds {

	public static ObjectId calculateFor(String data) {
		return calculateFor(data.getBytes());
	}

	public static ObjectId calculateFor(byte[] data) {
		var length = data.length;
		var stream = new ByteArrayInputStream(data);
		var md = SHA1.newInstance();
		md.update(Constants.encodedTypeString(Constants.OBJ_BLOB));
		md.update((byte) ' ');
		md.update(Constants.encodeASCII(length));
		md.update((byte) 0);
		var buf = new byte[8192];
		while (0 < length) {
			var next = stream.read(buf, 0, (int) Math.min(buf.length, length));
			if (next <= 0)
				return null;
			md.update(buf, 0, next);
			length -= next;
		}
		return md.toObjectId();
	}

	public static boolean equal(String data, ObjectId id) {
		return equal(data.getBytes(), id);
	}

	public static boolean equal(byte[] data, ObjectId id) {
		var newId = calculateFor(data);
		if (id == null) {
			id = ObjectId.zeroId();
		}
		return id.equals(newId);
	}

	public static boolean isNullOrZero(ObjectId id) {
		return id == null || id.equals(ObjectId.zeroId());
	}

}
