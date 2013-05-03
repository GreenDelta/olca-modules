package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.ILanguageCode;

/**
 * Contains the administrative information about the dataset at issue: type of
 * dataset (unit process, elementary flow, impact category, multi-output
 * process) timestamp, version and internalVersion number as well as language
 * and localLanguage code.
 * 
 * <p>
 * Java class for TDataSetInformation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TDataSetInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="5"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="impactAssessmentResult" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="timestamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="version" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}float">
 *             &lt;pattern value="\d{1,2} ?\.?\d{0,2}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="internalVersion" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}float">
 *             &lt;pattern value="\d{1,2}\.\d{1,2}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="energyValues" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="2"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="languageCode" type="{http://www.EcoInvent.org/EcoSpold01Impact}TISOLanguageCode" default="en" />
 *       &lt;attribute name="localLanguageCode" type="{http://www.EcoInvent.org/EcoSpold01Impact}TISOLanguageCode" default="de" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TDataSetInformation")
class ImpactDataSetInformation implements Serializable, IDataSetInformation {

	private final static long serialVersionUID = 1L;
	@XmlAttribute(name = "type", required = true)
	protected int type;
	@XmlAttribute(name = "impactAssessmentResult", required = true)
	protected boolean impactAssessmentResult;
	@XmlAttribute(name = "timestamp", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar timestamp;
	@XmlAttribute(name = "version", required = true)
	protected float version;
	@XmlAttribute(name = "internalVersion", required = true)
	protected float internalVersion;
	@XmlAttribute(name = "energyValues", required = true)
	protected int energyValues;
	@XmlAttribute(name = "languageCode")
	protected ImpactLanguageCode languageCode;
	@XmlAttribute(name = "localLanguageCode")
	protected ImpactLanguageCode localLanguageCode;

	/**
	 * Gets the value of the type property.
	 * 
	 */
	@Override
	public int getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 */
	@Override
	public void setType(int value) {
		this.type = value;
	}

	/**
	 * Gets the value of the impactAssessmentResult property.
	 * 
	 */
	@Override
	public boolean isImpactAssessmentResult() {
		return impactAssessmentResult;
	}

	/**
	 * Sets the value of the impactAssessmentResult property.
	 * 
	 */
	@Override
	public void setImpactAssessmentResult(boolean value) {
		this.impactAssessmentResult = value;
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
	 * Gets the value of the version property.
	 * 
	 */
	@Override
	public float getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 */
	@Override
	public void setVersion(float value) {
		this.version = value;
	}

	/**
	 * Gets the value of the internalVersion property.
	 * 
	 */
	@Override
	public float getInternalVersion() {
		return internalVersion;
	}

	/**
	 * Sets the value of the internalVersion property.
	 * 
	 */
	@Override
	public void setInternalVersion(float value) {
		this.internalVersion = value;
	}

	/**
	 * Gets the value of the energyValues property.
	 * 
	 */
	@Override
	public int getEnergyValues() {
		return energyValues;
	}

	/**
	 * Sets the value of the energyValues property.
	 * 
	 */
	@Override
	public void setEnergyValues(int value) {
		this.energyValues = value;
	}

	/**
	 * Gets the value of the languageCode property.
	 * 
	 * @return possible object is {@link ImpactLanguageCode }
	 * 
	 */
	@Override
	public ILanguageCode getLanguageCode() {
		if (languageCode == null)
			return ImpactLanguageCode.EN;
		return languageCode;
	}

	/**
	 * Sets the value of the languageCode property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactLanguageCode }
	 * 
	 */
	@Override
	public void setLanguageCode(ILanguageCode value) {
		if (value instanceof ImpactLanguageCode) {
			this.languageCode = (ImpactLanguageCode) value;
		} else {
			this.languageCode = ImpactLanguageCode.fromValue(value.value());
		}
	}

	/**
	 * Gets the value of the localLanguageCode property.
	 * 
	 * @return possible object is {@link ImpactLanguageCode }
	 * 
	 */
	@Override
	public ILanguageCode getLocalLanguageCode() {
		if (localLanguageCode == null)
			return ImpactLanguageCode.DE;
		return localLanguageCode;
	}

	/**
	 * Sets the value of the localLanguageCode property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactLanguageCode }
	 * 
	 */
	@Override
	public void setLocalLanguageCode(ILanguageCode value) {
		if (value instanceof ImpactLanguageCode) {
			this.localLanguageCode = (ImpactLanguageCode) value;
		} else {
			this.localLanguageCode = ImpactLanguageCode
					.fromValue(value.value());
		}
	}

}
