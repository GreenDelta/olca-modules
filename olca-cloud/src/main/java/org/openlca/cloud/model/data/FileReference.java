package org.openlca.cloud.model.data;

import java.io.Serializable;

import org.openlca.core.model.ModelType;

public class FileReference implements Serializable {

	private static final long serialVersionUID = -6108676257021661077L;
	public String refId;
	public ModelType type;

	@Override
	public int hashCode() {
		return (type.name() + "_" + refId).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FileReference))
			return false;
		FileReference ref = (FileReference) obj;
		if (ref.type != type)
			return false;
		return ref.refId.equals(refId);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " - refId: " + refId + ", type: " + type.name();
	}

}
