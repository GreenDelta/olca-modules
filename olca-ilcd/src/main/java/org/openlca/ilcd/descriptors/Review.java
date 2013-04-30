package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}scope" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}dataQualityIndicators" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}reviewDetails" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}otherReviewDetails" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}reviewer" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.ilcd-network.org/ILCD/ServiceAPI}TypeOfReviewValues" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "scope", "dataQualityIndicators",
		"reviewDetails", "otherReviewDetails", "reviewer" })
@XmlRootElement(name = "review")
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;
	protected List<Scope> scope;
	protected DataQualityIndicators dataQualityIndicators;
	protected LangString reviewDetails;
	protected LangString otherReviewDetails;
	protected DataSetReference reviewer;
	@XmlAttribute(name = "type", required = true)
	protected TypeOfReviewValues type;

	/**
	 * Gets the value of the scope property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the scope property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getScope().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Scope }
	 * 
	 * 
	 */
	public List<Scope> getScope() {
		if (scope == null) {
			scope = new ArrayList<>();
		}
		return this.scope;
	}

	/**
	 * Gets the value of the dataQualityIndicators property.
	 * 
	 * @return possible object is {@link DataQualityIndicators }
	 * 
	 */
	public DataQualityIndicators getDataQualityIndicators() {
		return dataQualityIndicators;
	}

	/**
	 * Sets the value of the dataQualityIndicators property.
	 * 
	 * @param value
	 *            allowed object is {@link DataQualityIndicators }
	 * 
	 */
	public void setDataQualityIndicators(DataQualityIndicators value) {
		this.dataQualityIndicators = value;
	}

	/**
	 * Gets the value of the reviewDetails property.
	 * 
	 * @return possible object is {@link LangString }
	 * 
	 */
	public LangString getReviewDetails() {
		return reviewDetails;
	}

	/**
	 * Sets the value of the reviewDetails property.
	 * 
	 * @param value
	 *            allowed object is {@link LangString }
	 * 
	 */
	public void setReviewDetails(LangString value) {
		this.reviewDetails = value;
	}

	/**
	 * Gets the value of the otherReviewDetails property.
	 * 
	 * @return possible object is {@link LangString }
	 * 
	 */
	public LangString getOtherReviewDetails() {
		return otherReviewDetails;
	}

	/**
	 * Sets the value of the otherReviewDetails property.
	 * 
	 * @param value
	 *            allowed object is {@link LangString }
	 * 
	 */
	public void setOtherReviewDetails(LangString value) {
		this.otherReviewDetails = value;
	}

	/**
	 * Gets the value of the reviewer property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getReviewer() {
		return reviewer;
	}

	/**
	 * Sets the value of the reviewer property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setReviewer(DataSetReference value) {
		this.reviewer = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link TypeOfReviewValues }
	 * 
	 */
	public TypeOfReviewValues getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link TypeOfReviewValues }
	 * 
	 */
	public void setType(TypeOfReviewValues value) {
		this.type = value;
	}

}
