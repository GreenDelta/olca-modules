package org.openlca.simapro.csv.model;


/**
 * Simple implementation of {@link ISystemDescriptionEntry}
 * 
 * @author Sebastian Greve
 * 
 */
public class SPSystemDescriptionEntry {

	/**
	 * A comment to the system description
	 */
	private String comment;

	/**
	 * The system description the entry refers to
	 */
	private SPSystemDescription systemDescription;

	/**
	 * Creates a new system description entry
	 * 
	 * @param systemDescription
	 *            The system description the entry refers to
	 */
	public SPSystemDescriptionEntry(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

	/**
	 * Creates a new system description entry
	 * 
	 * @param systemDescription
	 *            The system description the entry refers to
	 * @param comment
	 *            A comment to the system description
	 */
	public SPSystemDescriptionEntry(SPSystemDescription systemDescription,
			String comment) {
		this.systemDescription = systemDescription;
		this.comment = comment;
	}

	/**
	 * Getter of the comment
	 * 
	 * @return An optional additional comment to the system description
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Getter of the system description of the process
	 * 
	 * @see SPSystemDescription
	 * @return The system description of the process
	 */
	public SPSystemDescription getSystemDescription() {
		return systemDescription;
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
	 * Setter of the system description
	 * 
	 * @param systemDescription
	 *            The new system description
	 */
	public void setSystemDescription(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

}
