package org.openlca.simapro.csv.model;


/**
 * This class represents a literature reference entry in the process
 * documentation
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPLiteratureReferenceEntry {

	private String comment;
	private SPLiteratureReference literatureReference;

	public SPLiteratureReferenceEntry(SPLiteratureReference reference) {
		this.literatureReference = reference;
	}

	public SPLiteratureReferenceEntry(SPLiteratureReference reference,
			String comment) {
		this.literatureReference = reference;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public SPLiteratureReference getLiteratureReference() {
		return literatureReference;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setLiteratureReference(SPLiteratureReference literatureReference) {
		this.literatureReference = literatureReference;
	}

}
