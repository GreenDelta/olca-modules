package org.openlca.ecospold.internal.impact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IFlowData;
import org.openlca.ecospold.IMetaInformation;
import org.w3c.dom.Element;

/**
 * Contains information about one individual impact category. Information is
 * divided into metaInformation and flowData.
 * 
 * <p>
 * Java class for TDataset complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TDataset">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metaInformation" type="{http://www.EcoInvent.org/EcoSpold01Impact}TMetaInformation"/>
 *         &lt;element name="flowData" type="{http://www.EcoInvent.org/EcoSpold01Impact}TFlowData" maxOccurs="unbounded"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="number" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="internalSchemaVersion" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="generator" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString255" />
 *       &lt;attribute name="timestamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="validCompanyCodes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="validRegionalCodes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="validCategories" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="validUnits" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TDataset", propOrder = { "metaInformation", "flowData", "any" })
class ImpactDataSet implements Serializable, IDataSet {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ImpactMetaInformation.class)
	protected IMetaInformation metaInformation;
	@XmlElement(required = true, type = ImpactFactors.class)
	protected List<IFlowData> flowData;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
	@XmlAttribute(name = "number", required = true)
	protected int number;
	@XmlAttribute(name = "internalSchemaVersion")
	protected String internalSchemaVersion;
	@XmlAttribute(name = "generator", required = true)
	protected String generator;
	@XmlAttribute(name = "timestamp", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar timestamp;
	@XmlAttribute(name = "validCompanyCodes")
	protected String validCompanyCodes;
	@XmlAttribute(name = "validRegionalCodes")
	protected String validRegionalCodes;
	@XmlAttribute(name = "validCategories")
	protected String validCategories;
	@XmlAttribute(name = "validUnits")
	protected String validUnits;

	/**
	 * Gets the value of the metaInformation property.
	 * 
	 * @return possible object is {@link ImpactMetaInformation }
	 * 
	 */
	@Override
	public IMetaInformation getMetaInformation() {
		return metaInformation;
	}

	/**
	 * Sets the value of the metaInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactMetaInformation }
	 * 
	 */
	@Override
	public void setMetaInformation(IMetaInformation value) {
		this.metaInformation = value;
	}

	/**
	 * Gets the value of the flowData property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the flowData property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFlowData().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ImpactFactors }
	 * 
	 * 
	 */
	@Override
	public List<IFlowData> getFlowData() {
		if (flowData == null) {
			flowData = new ArrayList<>();
		}
		return this.flowData;
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

	/**
	 * Gets the value of the number property.
	 * 
	 */
	@Override
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the value of the number property.
	 * 
	 */
	@Override
	public void setNumber(int value) {
		this.number = value;
	}

	/**
	 * Gets the value of the internalSchemaVersion property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getInternalSchemaVersion() {
		return internalSchemaVersion;
	}

	/**
	 * Sets the value of the internalSchemaVersion property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setInternalSchemaVersion(String value) {
		this.internalSchemaVersion = value;
	}

	/**
	 * Gets the value of the generator property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getGenerator() {
		return generator;
	}

	/**
	 * Sets the value of the generator property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setGenerator(String value) {
		this.generator = value;
	}

	/**
	 * Gets the value of the timestamp property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	@Override
	public XMLGregorianCalendar getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the value of the timestamp property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	@Override
	public void setTimestamp(XMLGregorianCalendar value) {
		this.timestamp = value;
	}

	/**
	 * Gets the value of the validCompanyCodes property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValidCompanyCodes() {
		return validCompanyCodes;
	}

	/**
	 * Sets the value of the validCompanyCodes property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValidCompanyCodes(String value) {
		this.validCompanyCodes = value;
	}

	/**
	 * Gets the value of the validRegionalCodes property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValidRegionalCodes() {
		return validRegionalCodes;
	}

	/**
	 * Sets the value of the validRegionalCodes property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValidRegionalCodes(String value) {
		this.validRegionalCodes = value;
	}

	/**
	 * Gets the value of the validCategories property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValidCategories() {
		return validCategories;
	}

	/**
	 * Sets the value of the validCategories property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValidCategories(String value) {
		this.validCategories = value;
	}

	/**
	 * Gets the value of the validUnits property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValidUnits() {
		return validUnits;
	}

	/**
	 * Sets the value of the validUnits property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValidUnits(String value) {
		this.validUnits = value;
	}

}
