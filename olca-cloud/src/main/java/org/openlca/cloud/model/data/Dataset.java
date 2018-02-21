package org.openlca.cloud.model.data;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.openlca.cloud.util.NullSafe;
import org.openlca.core.model.ModelType;

public class Dataset extends FileReference implements Serializable {

	private static final long serialVersionUID = -5394277610226365725L;
	public String version;
	public long lastChange;
	public String name;
	public List<String> categories;
	public String categoryRefId;
	// used for categories
	public ModelType categoryType;

	public String getHashId() {
		String fullId = getFullId();
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(fullId.getBytes());
			char[] md5Chars = Hex.encodeHex(digest);
			return new String(md5Chars);
		} catch (NoSuchAlgorithmException e) {
			// can be ignored
			return null;
		}
	}

	private String getFullId() {
		String lastChange = Long.toString(this.lastChange);
		int length = type.name().length() + refId.length() + version.length() + lastChange.length();
		StringBuilder fullId = new StringBuilder(length);
		fullId.append(type.name());
		fullId.append(refId);
		fullId.append(version);
		fullId.append(lastChange);
		return fullId.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Dataset))
			return false;
		Dataset other = (Dataset) obj;
		if (!NullSafe.equal(type, other.type))
			return false;
		if (!NullSafe.equal(refId, other.refId))
			return false;
		if (!NullSafe.equal(version, other.version))
			return false;
		if (!NullSafe.equal(lastChange, other.lastChange))
			return false;
		return true;
	}

	public FileReference asFileReference() {
		FileReference ref = new FileReference();
		ref.type = type;
		ref.refId = refId;
		return ref;
	}

	@Override
	public String toString() {
		String value = super.toString() + ", version: " + version + ", lastChange: " + lastChange + ", name: " + name
				+ ", categories: ";
		if (categories != null && categories.size() > 0)
			for (String category : categories)
				value += "/" + category;
		if (type == ModelType.CATEGORY)
			value += ", categoryType: " + categoryType.name();
		if (categoryRefId != null) {
			value += ", categoryRefId: " + categoryRefId;
		}
		return value;
	}
}
