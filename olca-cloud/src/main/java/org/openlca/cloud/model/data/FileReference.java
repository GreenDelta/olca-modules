package org.openlca.cloud.model.data;

import java.io.Serializable;

import org.openlca.core.model.ModelType;

public class FileReference implements Serializable {

	private static final long serialVersionUID = -6108676257021661077L;
	public String refId;
	public ModelType type;

}
