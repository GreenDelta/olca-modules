package org.openlca.simapro.csv.model;


/**
 * This class represents a literature reference entry in the process
 * documentation
 */
public class SPLiteratureReferenceEntry {

	/**
	 * A comment to the entry
	 */
	private String comment;

	/**
	 * The literature reference the entry refers to
	 */
	private SPLiteratureReference literatureReference;

	/**
	 * Creates a new literature reference entry
	 * 
	 * @param reference
	 *            The literature reference the entry refers to
	 */
	public SPLiteratureReferenceEntry(SPLiteratureReference reference) {
		this.literatureReference = reference;
	}

	/**
	 * Creates a new literature reference entry
	 * 
	 * @param reference
	 *            The literature reference the entry refers to
	 * @param comment
	 *            A comment to the entry
	 */
	public SPLiteratureReferenceEntry(SPLiteratureReference reference,
			String comment) {
		this.literatureReference = reference;
		this.comment = comment;
	}

	/**
	 * Getter of the comment
	 * 
	 * @return An additional comment to the literature reference entry
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Getter of the literature reference
	 * 
	 * @see SPLiteratureReference
	 * @return The literature reference of the entry
	 */
	public SPLiteratureReference getLiteratureReference() {
		return literatureReference;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Setter of the literature reference
	 * 
	 * @param literatureReference
	 *            The new literature reference
	 */
	public void setLiteratureReference(SPLiteratureReference literatureReference) {
		this.literatureReference = literatureReference;
	}

}
