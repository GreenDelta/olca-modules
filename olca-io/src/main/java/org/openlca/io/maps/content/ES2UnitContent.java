package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;

public class ES2UnitContent implements IMappingContent {

	private String id;
	private String name;
	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getKey() {
		return KeyGen.get(id + name);
	}

}
