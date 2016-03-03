package org.openlca.geo.kml;

public enum FeatureType {

	EMPTY(false),

	POINT(false),

	LINE(false),

	POLYGON(false),

	MULTI_POINT(true),

	MULTI_LINE(true),

	MULTI_POLYGON(true),

	MULTI_GEOMETRY(true);

	private boolean multi;

	private FeatureType(boolean multi) {
		this.multi = multi;
	}

	public boolean isMulti() {
		return multi;
	}

}
