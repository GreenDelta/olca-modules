package org.openlca.simapro.csv.model;


/**
 * Simple implementation of {@link ISystemDescriptionEntry}
 */
public class SPSystemDescriptionEntry {

	private String comment;
	private SPSystemDescription systemDescription;

	public SPSystemDescriptionEntry(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

	public SPSystemDescriptionEntry(SPSystemDescription systemDescription,
			String comment) {
		this.systemDescription = systemDescription;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public SPSystemDescription getSystemDescription() {
		return systemDescription;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setSystemDescription(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

}
