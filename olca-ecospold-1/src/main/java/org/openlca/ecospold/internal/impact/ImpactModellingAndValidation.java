package org.openlca.ecospold.internal.impact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IModellingAndValidation;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.IValidation;
import org.w3c.dom.Element;

/**
 * Contains metainformation about how impact categories are modelled and about
 * the review/validation of the dataset.
 * 
 * <p>
 * Java class for TModellingAndValidation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TModellingAndValidation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="representativeness" type="{http://www.EcoInvent.org/EcoSpold01Impact}TRepresentativeness" maxOccurs="0" minOccurs="0"/>
 *         &lt;element name="source" type="{http://www.EcoInvent.org/EcoSpold01Impact}TSource" maxOccurs="unbounded"/>
 *         &lt;element name="validation" type="{http://www.EcoInvent.org/EcoSpold01Impact}TValidation" minOccurs="0"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TModellingAndValidation", propOrder = { "source",
		"validation", "any" })
class ImpactModellingAndValidation implements Serializable,
		IModellingAndValidation {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ImpactSource.class)
	protected List<ISource> source;
	@XmlElement(type = ImpactValidation.class)
	protected IValidation validation;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	/**
	 * Gets the value of the source property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the source property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSource().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ImpactSource }
	 * 
	 * 
	 */
	@Override
	public List<ISource> getSource() {
		if (source == null) {
			source = new ArrayList<>();
		}
		return this.source;
	}

	/**
	 * Gets the value of the validation property.
	 * 
	 * @return possible object is {@link ImpactValidation }
	 * 
	 */
	@Override
	public IValidation getValidation() {
		return validation;
	}

	/**
	 * Sets the value of the validation property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactValidation }
	 * 
	 */
	@Override
	public void setValidation(IValidation value) {
		this.validation = value;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * {@link Element }
	 * 
	 * 
	 */
	@Override
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return this.any;
	}

	@Override
	public IRepresentativeness getRepresentativeness() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRepresentativeness(IRepresentativeness value) {
		// TODO Auto-generated method stub

	}

}
