
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.RecommendationLevel;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecommendationType", propOrder = {
		"referenceToEntity",
		"level",
		"meaning"
})
public class Recommendation
		implements Serializable {

	private final static long serialVersionUID = 1L;
	protected List<Ref> referenceToEntity;
	protected RecommendationLevel level;

	@FreeText
	protected List<LangString> meaning;

	/**
	 * Gets the value of the referenceToEntity property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToEntity property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToEntity().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Ref }
	 * 
	 * 
	 */
	public List<Ref> getReferenceToEntity() {
		if (referenceToEntity == null) {
			referenceToEntity = new ArrayList<>();
		}
		return this.referenceToEntity;
	}

	/**
	 * Gets the value of the level property.
	 * 
	 * @return possible object is {@link RecommendationLevel }
	 * 
	 */
	public RecommendationLevel getLevel() {
		return level;
	}

	/**
	 * Sets the value of the level property.
	 * 
	 * @param value
	 *            allowed object is {@link RecommendationLevel }
	 * 
	 */
	public void setLevel(RecommendationLevel value) {
		this.level = value;
	}

	/**
	 * Gets the value of the meaning property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the meaning property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMeaning().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText
	 * }
	 * 
	 * 
	 */
	public List<LangString> getMeaning() {
		if (meaning == null) {
			meaning = new ArrayList<>();
		}
		return this.meaning;
	}

}
