package org.openlca.simapro.csv.model;


/**
 * This class represents a SimaPro literature reference
 * 
 * @author Sebastian Greve
 * 
 */
public class SPLiteratureReference {

	/**
	 * The category of the reference
	 */
	private String category;

	/**
	 * The content of the reference
	 */
	private String content;

	/**
	 * The name of the reference
	 */
	private String name;

	/**
	 * Creates a new literature reference
	 * 
	 * @param name
	 *            The name of the reference
	 * @param content
	 *            The content of the reference
	 * @param category
	 *            The category of the reference
	 */
	public SPLiteratureReference(String name, String content, String category) {
		this.name = name;
		this.content = content;
		this.category = category;
	}

	/**
	 * Getter of the content
	 * 
	 * @return The content of the literature reference
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the literature reference
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the category
	 * 
	 * @return The category of the literature reference
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Setter of the category
	 * 
	 * @param category
	 *            The new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Setter of the content
	 * 
	 * @param content
	 *            The new content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

}
