package org.openlca.ecospold.internal.process;

import java.io.Serializable;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.ILanguageCode;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

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
 *       &lt;attribute name="languageCode" type="{http://www.EcoInvent.org/EcoSpold01}TISOLanguageCode" default="en" />
 *       &lt;attribute name="localLanguageCode" type="{http://www.EcoInvent.org/EcoSpold01}TISOLanguageCode" default="de" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TDataSetInformation")
class DataSetInformation implements Serializable, IDataSetInformation {

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
	protected LanguageCode languageCode;
	@XmlAttribute(name = "localLanguageCode")
	protected LanguageCode localLanguageCode;

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
	 * @return possible object is {@link LanguageCode }
	 *
	 */
	@Override
	public ILanguageCode getLanguageCode() {
		if (languageCode == null)
			return LanguageCode.EN;
		return languageCode;
	}

	/**
	 * Sets the value of the languageCode property.
	 *
	 * @param value
	 *            allowed object is {@link LanguageCode }
	 *
	 */
	@Override
	public void setLanguageCode(ILanguageCode value) {
		if (value instanceof LanguageCode) {
			this.languageCode = (LanguageCode) value;
		} else {
			this.languageCode = LanguageCode.fromValue(value.value());
		}
	}

	/**
	 * Gets the value of the localLanguageCode property.
	 *
	 * @return possible object is {@link LanguageCode }
	 *
	 */
	@Override
	public ILanguageCode getLocalLanguageCode() {
		if (localLanguageCode == null)
			return LanguageCode.DE;
		return localLanguageCode;
	}

	/**
	 * Sets the value of the localLanguageCode property.
	 *
	 * @param value
	 *            allowed object is {@link LanguageCode }
	 *
	 */
	@Override
	public void setLocalLanguageCode(ILanguageCode value) {
		if (value instanceof LanguageCode) {
			this.localLanguageCode = (LanguageCode) value;
		} else {
			this.localLanguageCode = LanguageCode.fromValue(value.value());
		}
	}

}
