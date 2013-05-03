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
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IPerson;
import org.w3c.dom.Element;

/**
 * Contains the administrative information about the dataset at issue: type of
 * dataset (unit process, elementary flow, impact category, multi-output
 * process) timestamp, version and internalVersion number as well as language
 * and localLanguage code.
 * 
 * <p>
 * Java class for TAdministrativeInformation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TAdministrativeInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataEntryBy" type="{http://www.EcoInvent.org/EcoSpold01Impact}TDataEntryBy"/>
 *         &lt;element name="dataGeneratorAndPublication" type="{http://www.EcoInvent.org/EcoSpold01Impact}TDataGeneratorAndPublication"/>
 *         &lt;element name="person" type="{http://www.EcoInvent.org/EcoSpold01Impact}TPerson" maxOccurs="unbounded"/>
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
@XmlType(name = "TAdministrativeInformation", propOrder = { "dataEntryBy",
		"dataGeneratorAndPublication", "person", "any" })
class ImpactAdministrativeInformation implements Serializable,
		IAdministrativeInformation {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ImpactDataEntryBy.class)
	protected IDataEntryBy dataEntryBy;
	@XmlElement(required = true, type = ImpactDataGeneratorAndPublication.class)
	protected IDataGeneratorAndPublication dataGeneratorAndPublication;
	@XmlElement(required = true, type = ImpactPerson.class)
	protected List<IPerson> person;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	/**
	 * Gets the value of the dataEntryBy property.
	 * 
	 * @return possible object is {@link ImpactDataEntryBy }
	 * 
	 */
	@Override
	public IDataEntryBy getDataEntryBy() {
		return dataEntryBy;
	}

	/**
	 * Sets the value of the dataEntryBy property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactDataEntryBy }
	 * 
	 */
	@Override
	public void setDataEntryBy(IDataEntryBy value) {
		this.dataEntryBy = value;
	}

	/**
	 * Gets the value of the dataGeneratorAndPublication property.
	 * 
	 * @return possible object is {@link ImpactDataGeneratorAndPublication }
	 * 
	 */
	@Override
	public IDataGeneratorAndPublication getDataGeneratorAndPublication() {
		return dataGeneratorAndPublication;
	}

	/**
	 * Sets the value of the dataGeneratorAndPublication property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactDataGeneratorAndPublication }
	 * 
	 */
	@Override
	public void setDataGeneratorAndPublication(
			IDataGeneratorAndPublication value) {
		this.dataGeneratorAndPublication = value;
	}

	/**
	 * Gets the value of the person property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the person property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPerson().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ImpactPerson }
	 * 
	 * 
	 */
	@Override
	public List<IPerson> getPerson() {
		if (person == null) {
			person = new ArrayList<>();
		}
		return this.person;
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
