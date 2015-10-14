package com.greendelta.cloud.model.data;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.openlca.core.model.ModelType;

import com.greendelta.cloud.util.NullSafe;

public class DatasetDescriptor implements Serializable {

	private static final long serialVersionUID = -5394277610226365725L;
	private ModelType type;
	private String refId;
	private String version;
	private long lastChange;
	private String name;
	private String fullPath;
	private String categoryRefId;
	// used for categories
	private ModelType categoryType;

	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

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
		int length = type.name().length() + refId.length() + version.length()
				+ lastChange.length();
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
		if (!(obj instanceof DatasetDescriptor))
			return false;
		DatasetDescriptor other = (DatasetDescriptor) obj;
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
