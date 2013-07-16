package org.openlca.core.model.lean;

import org.openlca.core.model.ModelType;

public class CategoryDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = 1232584507113334297L;

	private String shortPath;

	public CategoryDescriptor() {
		setType(ModelType.CATEGORY);
	}

	public String getShortPath() {
		return shortPath;
	}

	public void setShortPath(String shortPath) {
		this.shortPath = shortPath;
	}

}
