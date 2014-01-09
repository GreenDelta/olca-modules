package org.openlca.io.maps.content;

import org.json.simple.parser.ParseException;

public abstract class AbstractMapContent {

	protected String olcaId;
	protected String mapType;
	protected boolean forImport;

	public String getOlcaId() {
		return olcaId;
	}

	public void setOlcaId(String olcaId) {
		this.olcaId = olcaId;
	}

	public String getMapType() {
		return mapType;
	}

	public void setForImport(boolean forImport) {
		this.forImport = forImport;
	}

	public boolean isForImport() {
		return forImport;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractMapContent))
			return false;
		AbstractMapContent content = (AbstractMapContent) obj;
		if (!equalsWithNull(content.getMapType(), mapType))
			return false;
		if (!equalsWithNull(content.getOlcaId(), olcaId))
			return false;
		if (!equalsWithNull(content.toJson(), toJson()))
			return false;
		if (Boolean.compare(content.isForImport(), forImport) != 0)
			return false;
		return true;
	}

	private boolean equalsWithNull(String s1, String s2) {
		if (s1 == null && s2 == null)
			return true;
		if (s1 != null && s2 != null && s1.equals(s2))
			return true;
		return false;
	}

	public abstract void fromJson(String json) throws ParseException;

	public abstract String toJson();

}
