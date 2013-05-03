package org.openlca.ecospold.internal.impact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IAdministrativeInformation;
import org.openlca.ecospold.IMetaInformation;
import org.openlca.ecospold.IModellingAndValidation;
import org.openlca.ecospold.IProcessInformation;
import org.w3c.dom.Element;

/**
 * Contains information about the impact category (its name, unit,
 * classification, geography, time etc.), about validation details and about
 * dataset administration (version number, kind of dataset, language).
 * 
 * <p>
 * Java class for TMetaInformation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TMetaInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processInformation" type="{http://www.EcoInvent.org/EcoSpold01Impact}TProcessInformation"/>
 *         &lt;element name="modellingAndValidation" type="{http://www.EcoInvent.org/EcoSpold01Impact}TModellingAndValidation"/>
 *         &lt;element name="administrativeInformation" type="{http://www.EcoInvent.org/EcoSpold01Impact}TAdministrativeInformation"/>
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
@XmlType(name = "TMetaInformation", propOrder = { "processInformation",
		"modellingAndValidation", "administrativeInformation", "any" })
class ImpactMetaInformation implements Serializable, IMetaInformation {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ImpactProcessInformation.class)
	protected IProcessInformation processInformation;
	@XmlElement(required = true, type = ImpactModellingAndValidation.class)
	protected IModellingAndValidation modellingAndValidation;
	@XmlElement(required = true, type = ImpactAdministrativeInformation.class)
	protected IAdministrativeInformation administrativeInformation;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	/**
	 * Gets the value of the processInformation property.
	 * 
	 * @return possible object is {@link ImpactProcessInformation }
	 * 
	 */
	@Override
	public IProcessInformation getProcessInformation() {
		return processInformation;
	}

	/**
	 * Sets the value of the processInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactProcessInformation }
	 * 
	 */
	@Override
	public void setProcessInformation(IProcessInformation value) {
		this.processInformation = value;
	}

	/**
	 * Gets the value of the modellingAndValidation property.
	 * 
	 * @return possible object is {@link ImpactModellingAndValidation }
	 * 
	 */
	@Override
	public IModellingAndValidation getModellingAndValidation() {
		return modellingAndValidation;
	}

	/**
	 * Sets the value of the modellingAndValidation property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactModellingAndValidation }
	 * 
	 */
	@Override
	public void setModellingAndValidation(IModellingAndValidation value) {
		this.modellingAndValidation = value;
	}

	/**
	 * Gets the value of the administrativeInformation property.
	 * 
	 * @return possible object is {@link ImpactAdministrativeInformation }
	 * 
	 */
	@Override
	public IAdministrativeInformation getAdministrativeInformation() {
		return administrativeInformation;
	}

	/**
	 * Sets the value of the administrativeInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactAdministrativeInformation }
	 * 
	 */
	@Override
	public void setAdministrativeInformation(IAdministrativeInformation value) {
		this.administrativeInformation = value;
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

}
