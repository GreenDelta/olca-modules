package org.openlca.cloud.model.data;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.openlca.core.model.ModelType;

import org.openlca.cloud.util.NullSafe;

public class Dataset extends FileReference implements Serializable {

	private static final long serialVersionUID = -5394277610226365725L;
	private String version;
	private long lastChange;
	private String name;
	private String fullPath;
	private String categoryRefId;
	// used for categories
	private ModelType categoryType;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getLastChange() {
		return lastChange;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getCategoryRefId() {
		return categoryRefId;
	}

	public void setCategoryRefId(String categoryRefId) {
		this.categoryRefId = categoryRefId;
	}

	public ModelType getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(ModelType categoryType) {
		this.categoryType = categoryType;
	}

	public String getHashId() {
		String fullId = getFullId();
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(
					fullId.getBytes());
			char[] md5Chars = Hex.encodeHex(digest);
			return new String(md5Chars);
		} catch (NoSuchAlgorithmException e) {
			// can be ignored
			return null;
		}
	}

	private String getFullId() {
		String lastChange = Long.toString(this.lastChange);
		int length = getType().name().length() + getRefId().length() + version.length()
				+ lastChange.length();
		StringBuilder fullId = new StringBuilder(length);
		fullId.append(getType().name());
		fullId.append(getRefId());
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
		if (!NullSafe.equal(getType(), other.getType()))
			return false;
		if (!NullSafe.equal(getRefId(), other.getRefId()))
			return false;
		if (!NullSafe.equal(getVersion(), other.getVersion()))
			return false;
		if (!NullSafe.equal(getLastChange(), other.getLastChange()))
			return false;
		return true;
	}

}
