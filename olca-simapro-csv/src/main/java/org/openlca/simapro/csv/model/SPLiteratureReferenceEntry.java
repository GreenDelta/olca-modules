package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;


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
	private LiteratureReferenceBlock literatureReference;

	public SPLiteratureReferenceEntry(LiteratureReferenceBlock reference) {
		this.literatureReference = reference;
	}

	public SPLiteratureReferenceEntry(LiteratureReferenceBlock reference,
			String comment) {
		this.literatureReference = reference;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public LiteratureReferenceBlock getLiteratureReference() {
		return literatureReference;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setLiteratureReference(LiteratureReferenceBlock literatureReference) {
		this.literatureReference = literatureReference;
	}

}
