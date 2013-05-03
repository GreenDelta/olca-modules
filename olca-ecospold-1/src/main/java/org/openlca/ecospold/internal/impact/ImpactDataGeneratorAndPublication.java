package org.openlca.ecospold.internal.impact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.ICountryCode;
import org.openlca.ecospold.IDataGeneratorAndPublication;

/**
 * Contains information about who compiled for and entered data into the
 * database. Furthermore contains information about kind of publication
 * underlying the dataset and the accessibility of the dataset.
 * 
 * <p>
 * Java class for TDataGeneratorAndPublication complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TDataGeneratorAndPublication">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="person" use="required" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="dataPublishedIn" default="0">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="2"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="referenceToPublishedSource" type="{http://www.EcoInvent.org/EcoSpold01Impact}TIndexNumber" />
 *       &lt;attribute name="copyright" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="accessRestrictedTo">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="3"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="companyCode" type="{http://www.EcoInvent.org/EcoSpold01Impact}TCompanyCode" />
 *       &lt;attribute name="countryCode" type="{http://www.EcoInvent.org/EcoSpold01Impact}TISOCountryCode" />
 *       &lt;attribute name="pageNumbers" type="{http://www.EcoInvent.org/EcoSpold01Impact}TString30" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TDataGeneratorAndPublication")
class ImpactDataGeneratorAndPublication implements Serializable,
		IDataGeneratorAndPublication {

	private final static long serialVersionUID = 1L;
	@XmlAttribute(name = "person", required = true)
	protected int person;
	@XmlAttribute(name = "dataPublishedIn")
	protected Integer dataPublishedIn;
	@XmlAttribute(name = "referenceToPublishedSource")
	protected Integer referenceToPublishedSource;
	@XmlAttribute(name = "copyright", required = true)
	protected boolean copyright;
	@XmlAttribute(name = "accessRestrictedTo")
	protected Integer accessRestrictedTo;
	@XmlAttribute(name = "companyCode")
	protected String companyCode;
	@XmlAttribute(name = "countryCode")
	protected ImpactCountryCode countryCode;
	@XmlAttribute(name = "pageNumbers")
	protected String pageNumbers;

	/**
	 * Gets the value of the person property.
	 * 
	 */
	@Override
	public int getPerson() {
		return person;
	}

	/**
	 * Sets the value of the person property.
	 * 
	 */
	@Override
	public void setPerson(int value) {
		this.person = value;
	}

	/**
	 * Gets the value of the dataPublishedIn property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	@Override
	public int getDataPublishedIn() {
		if (dataPublishedIn == null)
			return 0;
		return dataPublishedIn;
	}

	/**
	 * Sets the value of the dataPublishedIn property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	@Override
	public void setDataPublishedIn(Integer value) {
		this.dataPublishedIn = value;
	}

	/**
	 * Gets the value of the referenceToPublishedSource property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	@Override
	public Integer getReferenceToPublishedSource() {
		return referenceToPublishedSource;
	}

	/**
	 * Sets the value of the referenceToPublishedSource property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	@Override
	public void setReferenceToPublishedSource(Integer value) {
		this.referenceToPublishedSource = value;
	}

	/**
	 * Gets the value of the copyright property.
	 * 
	 */
	@Override
	public boolean isCopyright() {
		return copyright;
	}

	/**
	 * Sets the value of the copyright property.
	 * 
	 */
	@Override
	public void setCopyright(boolean value) {
		this.copyright = value;
	}

	/**
	 * Gets the value of the accessRestrictedTo property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	@Override
	public Integer getAccessRestrictedTo() {
		return accessRestrictedTo;
	}

	/**
	 * Sets the value of the accessRestrictedTo property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	@Override
	public void setAccessRestrictedTo(Integer value) {
		this.accessRestrictedTo = value;
	}

	/**
	 * Gets the value of the companyCode property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getCompanyCode() {
		return companyCode;
	}

	/**
	 * Sets the value of the companyCode property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setCompanyCode(String value) {
		this.companyCode = value;
	}

	/**
	 * Gets the value of the countryCode property.
	 * 
	 * @return possible object is {@link ImpactCountryCode }
	 * 
	 */
	@Override
	public ICountryCode getCountryCode() {
		return countryCode;
	}

	/**
	 * Sets the value of the countryCode property.
	 * 
	 * @param value
	 *            allowed object is {@link ImpactCountryCode }
	 * 
	 */
	@Override
	public void setCountryCode(ICountryCode value) {
		if (value instanceof ImpactCountryCode) {
			this.countryCode = (ImpactCountryCode) value;
		} else {
			this.countryCode = ImpactCountryCode.fromValue(value.value());
		}
	}

	/**
	 * Gets the value of the pageNumbers property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getPageNumbers() {
		return pageNumbers;
	}

	/**
	 * Sets the value of the pageNumbers property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setPageNumbers(String value) {
		this.pageNumbers = value;
	}

}
